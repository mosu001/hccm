package hccm.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jaamsim.ProcessFlow.SimEntity;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.Output;
import com.jaamsim.input.OutputHandle;
import com.jaamsim.input.ValueHandle;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;

import hccm.activities.Activity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
@SuppressWarnings("unused")
public class ActiveEntity extends SimEntity implements Entity {
	/**
	 * ?
	 */
	private ActiveEntity entityType;
	private Activity currentActivity;
	private double currentActivityStart;
	private ArrayList<String> activityStarts;
	private ArrayList<Double> activityStartTimes;
	
	{
		setEntityType(null);
		setCurrentActivity(null);
		setCurrentActivityStart(0.0);
		initActivityStarts();
		initActivityStartTimes();
	}
	
	@Output(name = "EntityType" ,
			unitType = DimensionlessUnit.class ,
			description = "The type of entity that this entity is",
		    sequence = 1)
	public String getEntityType(double simTime) {
		return getEntityType().getName();
	}
	
	@Output(name = "CurrentActivity" ,
			unitType = DimensionlessUnit.class ,
			description = "Current activity this entity is participating in",
		    sequence = 2)
	public String getCurrentActivity(double simTime) {
		return getCurrentActivity().getName();
	}
	
	@Output(name = "CurrentActivityStart" ,
			unitType = DimensionlessUnit.class ,
			description = "Start time of the current activity this entity is participating in",
		    sequence = 3)
	public double getCurrentActivityStart(double simTime) {
		return getCurrentActivityStart();
	}
	
	@Output(name = "ActivityStarts" ,
			unitType = DimensionlessUnit.class ,
			description = "The activities that the entity has started",
		    sequence = 4)
	public ArrayList<String> getActivityStarts(double simTime) {
		return getActivityStarts();
	}
	
	@Output(name = "ActivityStartTimes" ,
			unitType = DimensionlessUnit.class ,
			description = "The times that the entity has started activities",
		    sequence = 5)
	public ArrayList<Double> getActivityStartTimes(double simTime) {
		return getActivityStartTimes();
	}

	
	/**
	 * Helper function that converts a single entity to a list, for use with other functions
	 * @return a list of ActiveEntity objects
	 */
	public ArrayList<ActiveEntity> asList() {
		return new ArrayList<ActiveEntity>(Arrays.asList(this));
	}

	/**
	 * Getter function for entityType
	 * @return entityType
	 */
	public ActiveEntity getEntityType() {
		return entityType;
	}

	/**
	 * Setter function for entityType
	 * @param entityType
	 */
	public void setEntityType(Entity type) {
		this.entityType = (ActiveEntity)type;
	}

	/**
	 * Getter function for currentActivity
	 * @return currentActivity
	 */
	public Activity getCurrentActivity() {
		return currentActivity;
	}

	/**
	 * Setter function for currentActivity
	 * @param currentActivity
	 */
	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	
	/**
	 * Getter function for currentActivityStart
	 * @return currentActivityStart
	 */
	public double getCurrentActivityStart() {
		return currentActivityStart;
	}
	
	/**
	 * Setter function for currentActivityStart
	 * @param simTime
	 */
	public void setCurrentActivityStart(double simTime) {
		this.currentActivityStart = simTime;
	}
	
	/**
	 * Getter function for activityStarts
	 * @return currentActivityStart
	 */
	public ArrayList<String> getActivityStarts() {
		return activityStarts;
	}
	
	/**
	 * Setter function for activityStarts
	 * @param simTime
	 */
	public void initActivityStarts() {
		activityStarts = new ArrayList<String>();
	}
	
	/**
	 * Append function for activityStarts
	 * @param simTime
	 */
	public void addActivityStart(String actStart) {
		activityStarts.add(actStart);
	}
	
	/**
	 * Getter function for activityStartTimes
	 * @return currentActivityStart
	 */
	public ArrayList<Double> getActivityStartTimes() {
		return activityStartTimes;
	}
	
	/**
	 * Setter function for activityStartTimes
	 * @param simTime
	 */
	public void initActivityStartTimes() {
		activityStartTimes = new ArrayList<Double>();
	}
	
	/**
	 * Append function for activityStartTimes
	 * @param simTime
	 */
	public void addActivityStartTime(double actStartTime) {
		activityStartTimes.add(actStartTime);
	}
	
	/**
	 * Getter function for numeric attribute
	 * @return currentActivityStart
	 */
	public double getNumAttribute(String outputName, double simTime, double def) {
		ValueHandle output = this.getOutputHandle(outputName);
		if (output == null) {
			String msg = "Could not find output '%s' on entity of type '%s'\n"
				+ "The error occured in file: '%s', method: '%s', line: '%s'";
			throw new ErrorException(msg, outputName, this.getEntityType().getLocalName(),
					Thread.currentThread().getStackTrace()[2].getFileName(),
					Thread.currentThread().getStackTrace()[2].getMethodName(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
		} else {
			return output.getValueAsDouble(simTime, def);
		}
	}
	
	/**
	 * Setter function for numeric attribute
	 * @param simTime
	 */
	public void setNumAttribute(String outputName, double val, Class<? extends Unit> ut) {
		ExpResult eR = ExpResult.makeNumResult(val, ut);
		try {
			this.setAttribute(outputName, null, eR);
        } catch (ExpError e) {
        	String newMsg = e.getMessage() + "\n"
					+ "The error occured in file: '%s', method: '%s', line: '%s'";
			newMsg = String.format(newMsg, Thread.currentThread().getStackTrace()[2].getFileName(),
					Thread.currentThread().getStackTrace()[2].getMethodName(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
			throw new ErrorException(this, newMsg);
//        	throw new ErrorException(this, e);
		}
	}
	
	/**
	 * Getter function for string attribute
	 * @return currentActivityStart
	 */
	public String getStringAttribute(String outputName, double simTime) {
		ValueHandle output = this.getOutputHandle(outputName);
		if (output == null) {
			String msg = "Could not find output: '%s' on entity of type '%s'\n"
					+ "The error occured in file: '%s', method: '%s', line: '%s'";
			throw new ErrorException(msg, outputName, this.getEntityType().getLocalName(),
					Thread.currentThread().getStackTrace()[2].getFileName(),
					Thread.currentThread().getStackTrace()[2].getMethodName(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
		} else {
			return this.getOutputHandle(outputName).getValue(simTime, String.class);
		}
	}
	
	/**
	 * Setter function for string attribute
	 * @param simTime
	 */
	public void setStringAttribute(String outputName, String val) {
		ExpResult eR = ExpResult.makeStringResult(val);
		try {
			this.setAttribute(outputName, null, eR);
        } catch (ExpError e) {
        	String newMsg = e.getMessage() + "\n"
					+ "The error occured in file: '%s', method: '%s', line: '%s'";
			newMsg = String.format(newMsg, Thread.currentThread().getStackTrace()[2].getFileName(),
					Thread.currentThread().getStackTrace()[2].getMethodName(),
					Thread.currentThread().getStackTrace()[2].getLineNumber());
			throw new ErrorException(this, newMsg);
//        	throw new ErrorException(this, e);
		}
	}

	@Override
	public void earlyInit() {
		super.earlyInit();

		initActivityStarts();
		initActivityStartTimes();
	}
}
