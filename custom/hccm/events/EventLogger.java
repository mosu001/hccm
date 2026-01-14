package hccm.events;

import java.io.File;
import java.util.ArrayList;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.StringProviders.StringProvListInput;
import com.jaamsim.StringProviders.StringProvider;
import com.jaamsim.basicsim.FileEntity;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.BooleanInput;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.UnitTypeListInput;
import com.jaamsim.input.ValueInput;
import com.jaamsim.units.TimeUnit;
import com.jaamsim.units.Unit;

import hccm.entities.ActiveEntity;

public class EventLogger extends DisplayEntity {
	
	@Keyword(description = "If TRUE, an individual .log file will be created for each simulation "
            + "run and the suffix '-sNrM' will be added to each .log file name, "
            + "where N and M are the scenario and replication numbers for the run. "
            + "If FALSE, a single .log file will be created that contains the "
            + "outputs for all the simulation runs. "
            + "This input is ignored if multiple runs are to be executed and the "
            + "NumberOfThreads input is greater than one, in which case individual "
            + ".log files will be created.",
            exampleList = { "FALSE" })
	private final BooleanInput separateFiles;

	@Keyword(description = "If TRUE, log entries are recorded during the initialization period.",
	         exampleList = { "FALSE" })
	private final BooleanInput includeInitialization;

	@Keyword(description = "The time at which the log starts recording entries.",
	         exampleList = { "24.0 h" })
	private final ValueInput startTime;

	@Keyword(description = "The time at which the log stops recording entries.",
	         exampleList = { "8760.0 h" })
	private final ValueInput endTime;

	private FileEntity file;
	private double logTime;

	{
		active.setHidden(false);
		
		separateFiles = new BooleanInput("SeparateFiles", KEY_INPUTS, false);
		this.addInput(separateFiles);

		includeInitialization = new BooleanInput("IncludeInitialization", KEY_INPUTS, true);
		this.addInput(includeInitialization);

		startTime = new ValueInput("StartTime", KEY_INPUTS, 0.0d);
		startTime.setUnitType(TimeUnit.class);
		startTime.setValidRange(0.0d, Double.POSITIVE_INFINITY);
		this.addInput(startTime);

		endTime = new ValueInput("EndTime", KEY_INPUTS, Double.POSITIVE_INFINITY);
		endTime.setUnitType(TimeUnit.class);
		endTime.setValidRange(0.0d, Double.POSITIVE_INFINITY);
		this.addInput(endTime);
	}
	
	private DisplayEntity receivedEntity;

	public EventLogger() {}

	@Override
	public void earlyInit() {
		super.earlyInit();
		receivedEntity = null;

		logTime = 0.0d;

		// Close the file if it is already open
		JaamSimModel simModel = getJaamSimModel();
		if (file != null && simModel.isFirstRun()) {
			file.close();
			file = null;
		}

		if (!isActive())
			return;

		// Create the report file
		if (file == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("-").append(this.getName());
			if (isSeparateFiles()) {
				sb.append("-s").append(simModel.getScenarioNumber());
				sb.append("r").append(simModel.getReplicationNumber());
			}
			sb.append(".log");
			String fileName = simModel.getReportFileName(sb.toString());
			if (fileName == null)
				return;
			File f = new File(fileName);
			if (f.exists() && !f.delete())
				error("Cannot delete the existing log file %s", f);
			file = new FileEntity(simModel, f);
		}

		// Print the detailed run information to the file
		if (getJaamSimModel().isFirstRun())
			InputAgent.printReport(getSimulation(), file, 0.0d);

		// Print run number header if multiple runs are to be performed
		if (getJaamSimModel().isMultipleRuns()) {
			if (!getJaamSimModel().isFirstRun()) {
				file.format("%n");
			}
			file.format("%n%s%n", getJaamSimModel().getRunHeader());
		}

		// Print the title for each column
		// (a) Simulation time
		String unit = getJaamSimModel().getDisplayedUnit(TimeUnit.class);
		file.format("%nthis.SimTime/1[%s]", unit);

		// (b) Print at titles for any additional columns
		this.printColumnTitles(file);

		// (c) Print the mathematical expressions to be logged
		ArrayList<String> toks = new ArrayList<>();
		toks.add("Event");
		toks.add("EventTime");
		//dataSource.getValueTokens(toks);
		for (String str : toks) {
			if (str.equals("{") || str.equals("}"))
				continue;
			file.format("\t%s", str);
		}

		// Empty the output buffer
		file.flush();
	}
	
	protected boolean isSeparateFiles() {
		int numThreads = getJaamSimModel().getSimulation().getNumberOfThreads();
		return separateFiles.getValue() || numThreads > 1;
	}
	
	public void recordEntityEvents(DisplayEntity ent) {

		receivedEntity = ent;

		// Record the entry in the log
		this.recordLogEntry(getSimTime());
	}

	/**
	 * Writes an entry to the log file.
	 */
	protected void recordLogEntry(double simTime) {

		if (!isActive())
			return;

		// Skip the log entry if the log file has been closed at the end of the run duration
		if (file == null)
			return;

		// Skip the log entry if the run is still initializing
		if (!includeInitialization.getValue() && simTime < getSimulation().getInitializationTime())
			return;

		// Skip the log entry if it is outside the time range
		if (simTime < startTime.getValue() || simTime > endTime.getValue())
			return;

		// Record the time for the log entry
		logTime = simTime;
		
		JaamSimModel simModel = getJaamSimModel();
		String scenarioCode = simModel.getScenarioCode();
		int replication = simModel.getReplicationNumber();

		ArrayList<String> eventStarts = ((ActiveEntity) receivedEntity).getActivityStarts(simTime);
		ArrayList<Double> eventStartTimes = ((ActiveEntity) receivedEntity).getActivityStartTimes(simTime);
		
		// Write a new line for each event
		for (int i=0; i<eventStarts.size(); i++) {
			double factor = getJaamSimModel().getDisplayedUnitFactor(TimeUnit.class);
			file.format("%n%s", simTime/factor);
			file.format("\t%s", scenarioCode);
			file.format("\t%s", replication);

			// Write any additional columns for the log entry
			this.recordEntry(file, simTime);
			String str;
			str = eventStarts.get(i);
			file.format("\t%s", str);
			str = Double.toString((eventStartTimes.get(i)/factor));
			file.format("\t%s", str);
		
		}
		
		// Write the time for the log entry
		//double factor = Unit.getDisplayedUnitFactor(TimeUnit.class);
		//file.format("%n%s", simTime/factor);
//		double factor = getJaamSimModel().getDisplayedUnitFactor(TimeUnit.class);
//		file.format("%n%s", simTime/factor);

		// Write any additional columns for the log entry
		//this.recordEntry(file, simTime);

		
		
		// Write the expression values
		/*
		for (int i=0; i<dataSource.getListSize(); i++) {
			String str;
			try {
				StringProvider samp = dataSource.getValue().get(i);
				str = samp.getNextString(simTime);
			}
			catch (Exception e) {
				str = e.getMessage();
			}
			file.format("\t%s", str);
		}
		*/

		// If running in real time mode, empty the file buffer after each entity is logged
		if (!getJaamSimModel().isBatchRun() && getSimulation().isRealTime())
			file.flush();
	}

	protected double getStartTime() {
		return startTime.getValue();
	}

	protected double getEndTime() {
		return endTime.getValue();
	}

	protected void printColumnTitles(FileEntity file) {
		file.format("\t%s", "Scenario");
		file.format("\t%s", "Replication");
		file.format("\t%s", "this.obj");
	}

	protected void recordEntry(FileEntity file, double simTime) {
		file.format("\t%s", receivedEntity);
	}

	@Override
	public void doEnd() {
		super.doEnd();
		file.flush();

		// Close the report file
		if (getJaamSimModel().isLastRun()) {
			file.close();
			file = null;
		}
	}

	@Output(name = "LogTime",
	 description = "The simulation time at which the last log entry was made.",
	    unitType = TimeUnit.class)
	public double getLogTime(double simTime) {
		return logTime;
	}

}
