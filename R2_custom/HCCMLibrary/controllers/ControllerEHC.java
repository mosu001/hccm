package HCCMLibrary.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jaamsim.BasicObjects.InputValue;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProbabilityDistributions.Distribution;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.TimeSeries;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

import HCCMLibrary.controlactivity.HCCMControlActivity;
import HCCMLibrary.entities.HCCMActiveEntity;

public class ControllerEHC extends HCCMController {

	// Create needed entities
	DisplayEntity waitingroom = null;
	DisplayEntity treatmentroom1 = null;
	DisplayEntity treatmentroom2 = null;
	DisplayEntity triageroom = null;
	DisplayEntity testroom = null;
	DisplayEntity patientleave1 = null;
	DisplayEntity patientleave2 = null;
	DisplayEntity patientleave3 = null;
	DisplayEntity nurseleave = null;
	DisplayEntity doctorleave = null;
	DisplayEntity walkupdoctorroster = null;
	DisplayEntity appointmentdoctorroster = null;
	DisplayEntity controllerehc = null;
	DisplayEntity scheduledpatientfinished = null;
	DisplayEntity walkuppatientfinished = null;

	// Create needed variables
	Double waitingroomcapacity = null;
	Double simindex = null;
	Double changeshiftwalkupdoctor = null;
	Double changeshiftappointmentdoctor = null;

	// Create needed data Maps (utilisationTimes for this example)
	Map<String, Double> utilisationTimes = new HashMap<String, Double>();

	@Override
	public void earlyInit() {
		super.earlyInit();

		// Get needed entities
		waitingroom = getDisplayEntity("WaitingRoom");
		treatmentroom1 = getDisplayEntity("TreatmentRoom1");
		treatmentroom2 = getDisplayEntity("TreatmentRoom2");
		triageroom = getDisplayEntity("TriageRoom");
		testroom = getDisplayEntity("TestRoom");
		patientleave1 = getDisplayEntity("PatientLeaveEvent1");
		patientleave2 = getDisplayEntity("PatientLeaveEvent2");
		patientleave3 = getDisplayEntity("PatientLeaveEvent3");
		nurseleave = getDisplayEntity("NurseLeave");
		doctorleave = getDisplayEntity("DoctorLeave");
		walkupdoctorroster = getDisplayEntity("WalkUpDoctorRoster");
		appointmentdoctorroster = getDisplayEntity("AppointmentDoctorRoster");
		scheduledpatientfinished = getDisplayEntity("ScheduledPatientFinished");
		walkuppatientfinished = getDisplayEntity("WalkUpPatientFinished");
		controllerehc = this;

		// Get needed variables
		waitingroomcapacity = getInputValue("WaitingRoomCapacity");
		simindex = getSimulationRunIndex(0);
		
		// Time when WalkUpDoctorShift changes, send signal to controller
		changeshiftwalkupdoctor = getNextChange("WalkUpDoctorRoster");
		this.scheduleProcess(changeshiftwalkupdoctor, 5, "sendActivitySignal",controllerehc, walkupdoctorroster, walkupdoctorroster, "ChangeState");

		// Time when AppointmentDoctorShift changes, send signal to controller
		changeshiftappointmentdoctor = getNextChange("AppointmentDoctorRoster");
		this.scheduleProcess(changeshiftappointmentdoctor, 5, "sendActivitySignal",controllerehc, appointmentdoctorroster, appointmentdoctorroster, "ChangeState");
		
		// Clear data Maps (utilisationTimes for this example)
		utilisationTimes.clear();

	}

	@Override
	public void Controller(DisplayEntity activeEntity, DisplayEntity activity, String state){

		// WalkUp Patient start Activity at WaitingRoom
		if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			
			// Set the appropriate state (since the waiting room handles multiple purposes)
			if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.001) {
				((HCCMActiveEntity)walkuppatient).setPresentState("WaitForTriage");
			}
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.001) {
				((HCCMActiveEntity)walkuppatient).setPresentState("WaitForTest");
			}
			
			// If waiting room is full
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() >= waitingroomcapacity) {
				moveEntFromTo(walkuppatient,waitingroom,patientleave1);
			}
			// Else if not triaged and Triage nurse available
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && serverAvailable("TriageNurse",triageroom) ) {
				System.out.println("Sent end activity to list");
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
			// Else if triaged but not tested and needs test and Test nurse available
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && serverAvailable("TestNurse",testroom)) {
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
			// Finally, if ready for treatment (ie. triaged and either test completed or didnt need test)
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 || Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01)) {
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
		
		}
		// WalkUp Patient ends Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			
			// Patient needs to be triaged, Triage Nurse is available, WalkUp Patient ends Activity WaitingRoom
			if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && serverAvailable("TriageNurse",triageroom)) { 
				moveEntFromTo(walkuppatient,waitingroom,triageroom);
			}
			
			// Patient needs a test, Test Nurse is available, WalkUp Patient ends Activity WaitingRoom
			else if (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.001 && serverAvailable("TestNurse", testroom)) {
				moveEntFromTo(walkuppatient,waitingroom,testroom);
			}
			
			// WalkUp Doctor is available, patient has been triaged and either doesn't need a test or has completed it
			else if (serverAvailable("WalkUpDoctor",treatmentroom1) && Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01)) {
				moveEntFromTo(walkuppatient, waitingroom, treatmentroom1);
			}

			// Appointment Doctor is available, patient has been triaged and either doesn't need a test or has completed it
			else if ((serverAvailable("AppointmentDoctor",treatmentroom2) && Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01))) {
				moveEntFromTo(walkuppatient,waitingroom,treatmentroom2);
			}

		}
		
		// WalkUp Patient starts Activity at TriageRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TriageRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity triagenurse = getServerAvailable("TriageNurse",triageroom);
			makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("TriageNurse"));
			
			((HCCMActiveEntity)walkuppatient).setPresentState("Triage");
			
			
			if (simindex == 1 || simindex == 2) {
				double triagetime = 5*60;
				startScheduledActvitity(walkuppatient, triagenurse, triageroom, triagetime);
			}
			else if (simindex == 3 || simindex == 4) {
				double triagetime = getDistributionValue("TriageDist");
				startScheduledActvitity(walkuppatient, triagenurse, triageroom, triagetime);
			}
		}

		// WalkUp Patient ends Activity at TriageRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TriageRoom", "EndActivity")) {
			
			//DisplayEntity triagenurse = getDisplayEntity("TriageNurse");
			DisplayEntity triagenurse = ((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("TriageNurse");
			ExpResult r = ExpResult.makeNumResult(1.0, DimensionlessUnit.class);
			((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("WalkUpPatient").setAttribute("hasBeenTriaged", null, r);
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,triageroom,waitingroom);
			makeServerAvailable(triagenurse);
		}		
		
		// WalkUp Patient starts Activity at TestRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TestRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity testnurse = getDisplayEntity("TestNurse");
			testnurse = getServerAvailable("TestNurse",testroom);
			makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TestRoom")).getFirstForMatch("TestNurse"));
			
			((HCCMActiveEntity)walkuppatient).setPresentState("Test");
			
			if (simindex == 1 || simindex == 2) {
				double testtime = 10*60;
				startScheduledActvitity(walkuppatient, testnurse, testroom, testtime);	
			}
			else if (simindex == 3 || simindex == 4) {
				double testtime = getDistributionValue("TestDist");
				startScheduledActvitity(walkuppatient, testnurse, testroom, testtime);	
			}
			
		}

		// WalkUp Patient ends Activity at TestRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TestRoom", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity testnurse = getDisplayEntity("TestNurse");
			ExpResult r = ExpResult.makeNumResult(1.0, DimensionlessUnit.class);
			walkuppatient.setAttribute("hasBeenTested", null, r);
			moveEntFromTo(walkuppatient,testroom,waitingroom);
			makeServerAvailable(testnurse);
		}
		
		// WalkUp Patient starts Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom1", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity walkupdoctor = getServerAvailable("WalkUpDoctor",treatmentroom1);
			makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("WalkUpDoctor"));

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (walkuppatient, walkupdoctor, treatmentroom1, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("WalkupTreatDist");
				startScheduledActvitity (walkuppatient, walkupdoctor, treatmentroom1, treatmenttime);
			}
		}
		
		// WalkUp Patient starts Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom2", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity appointmentdoctor = getServerAvailable("AppointmentDoctor",treatmentroom2);
			makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("AppointmentDoctor"));

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (walkuppatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("AppointmentTreatDist");
				startScheduledActvitity (walkuppatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}
		}		
		
		// WalkUp Patient ends Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom1", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity walkupdoctor = getDisplayEntity("WalkUpDoctor");
			moveEntFromTo(walkuppatient,treatmentroom1,walkuppatientfinished);
			makeServerAvailable(walkupdoctor);
		}

		// WalkUp Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom2", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity appointmentdoctor = getDisplayEntity("AppointmentDoctor");
			moveEntFromTo(walkuppatient,treatmentroom2,scheduledpatientfinished);
			makeServerAvailable(appointmentdoctor);
		}
		
		// Scheduled Patient start Activity at WaitingRoom
		else if(happens(activeEntity, activity, state, "ScheduledPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			((HCCMActiveEntity)scheduledpatient).setPresentState("Wait");

			// Appointment Doctor is available, Scheduled Patient ends Activity WaitingRoom
			if  (serverAvailable("AppointmentDoctor",treatmentroom2) && scheduledpatient.getOutputHandle("AppointmentTime").getValueAsDouble(getSimTime(), 0.0)*60 >= getSimTime()) { 
				sendActivitySignalToList(scheduledpatient, waitingroom, "EndActivity");
			}
		}

		// Scheduled Patient end Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "WaitingRoom", "EndActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			moveEntFromTo(scheduledpatient,waitingroom,treatmentroom2);
		}

		// Scheduled Patient starts Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "TreatmentRoom2", "StartActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			DisplayEntity appointmentdoctor = getServerAvailable("AppointmentDoctor",treatmentroom2);

			((HCCMActiveEntity)scheduledpatient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (scheduledpatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("AppointmentTreatDist");
				startScheduledActvitity (scheduledpatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}

		}
		
		// Should also have functionality here for changing states of nurses, doctors

		// Scheduled Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "TreatmentRoom2", "EndActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			DisplayEntity appointmentdoctor = getDisplayEntity("AppointmentDoctor");
			moveEntFromTo(scheduledpatient,treatmentroom2,scheduledpatientfinished);
			makeServerAvailable(appointmentdoctor);

		}		

		// Shift of WalkUpDoctor Ends
		else if (happens(activeEntity, activity, state, "WalkUpDoctorRoster", "WalkUpDoctorRoster", "ChangeState")) {
			double WalkUpDoctorShift = getTimeSeriesValue("WalkUpDoctorRoster");
			if (WalkUpDoctorShift == 0) {
				while (serverAvailable("WalkUpDoctor",treatmentroom1)) {
					DisplayEntity walkupdoctor = getServerAvailable("WalkUpDoctor", treatmentroom1);
					moveEntFromTo(walkupdoctor,treatmentroom1, doctorleave);
				}
			}
			changeshiftwalkupdoctor = getNextChange("WalkUpDoctorRoster")-getSimTime();
			// Schedule next change
			this.scheduleProcess(changeshiftwalkupdoctor, 5, "sendActivitySignal",controllerehc, walkupdoctorroster, walkupdoctorroster, "ChangeState");
		}
		
		// Shift of AppointmentDoctor Ends
		else if (happens(activeEntity, activity, state, "AppointmentDoctorRoster", "AppointmentDoctorRoster", "ChangeState")) {
			double AppointmentDoctorShift = getTimeSeriesValue("AppointmentDoctorRoster");
			if (AppointmentDoctorShift == 0) {
				while (serverAvailable("AppointmentDoctor",treatmentroom2)) {
					DisplayEntity appointmentdoctor = getServerAvailable("AppointmentDoctor", treatmentroom2);
					moveEntFromTo(appointmentdoctor,treatmentroom2, doctorleave);
				}
			}
			changeshiftappointmentdoctor = getNextChange("AppointmentDoctorRoster")-getSimTime();// simTime;
			// Schedule next change
			this.scheduleProcess(changeshiftappointmentdoctor, 5, "sendActivitySignal",controllerehc, appointmentdoctorroster, appointmentdoctorroster, "ChangeState");
		}		
	}
	
	// Helper functions go here
		public void startScheduledActvitity (DisplayEntity customer, DisplayEntity server, DisplayEntity activity, double duration) {

			makeServerUnavailable(server);
			((HCCMActiveEntity)server).setPresentState("Working");

			this.scheduleProcess(duration, 5, "sendActivitySignalToList", customer, activity, "EndActivity");
			this.scheduleProcess(duration, 5, "sendActivitySignalToList", server, activity, "EndActivity");
		}

		public void removeEntFrom(DisplayEntity Ent, DisplayEntity From){
			((HCCMControlActivity)From).removeEntity(Ent);
		}

		public void moveEntFromTo(DisplayEntity Ent, DisplayEntity From ,DisplayEntity To) {
			((HCCMControlActivity)From).removeEntity(Ent);
			try {
				((Linkable)To).addEntity(Ent);	
			}
			catch (Exception e) {
				System.out.println("Null Pointer Exception: an entity name is likely misspelled in the controller logic.");
			}
		}

		public boolean happens(DisplayEntity active, DisplayEntity passive, String state, String ifactive, String ifpassive, String ifstate) {
			DisplayEntity ifpassiveEnt = getDisplayEntity(ifpassive);
			boolean happen = (active.getName().startsWith(ifactive) && passive.equals(ifpassiveEnt) && state.equals(ifstate));
			return happen;
		}

		public boolean serverAvailable(String server, Entity location) {

			ArrayList <String> matchEntities = ((HCCMControlActivity)location).getMatchValues(getSimTime());
			ArrayList<String> indexes = new ArrayList<String>();

			for(int i=0; i<matchEntities.size(); i++){
				if(matchEntities.get(i).equals(server) && ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i).getOutputHandle("ServerAvailable").getValue(getSimTime(), String.class).equals("1"))
				{               
					String id = Integer.toString(i);
					indexes.add(id);
				}
			}
			int serversAvailable = indexes.size();

			boolean serverAvailable = serversAvailable > 0;
			return serverAvailable;
		}

		public void makeServerAvailable(DisplayEntity server) {
			try {
				((HCCMActiveEntity)server).setPresentState("Idle");
			}
			catch (Exception e) {
				System.out.println(e);
			}
			ExpResult eR = ExpResult.makeStringResult("1");
			((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);
		}

		public void makeServerUnavailable(DisplayEntity server) {

			ExpResult eR = ExpResult.makeStringResult("0");
			((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);

		}

		public DisplayEntity getServerAvailable(String server, Entity location) {

			ArrayList <String> matchEntities = ((HCCMControlActivity)location).getMatchValues(getSimTime());
			DisplayEntity serverAvailableEntity = null;

			for(int i=0; i<matchEntities.size(); i++){
				if(matchEntities.get(i).equals(server) && ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i).getOutputHandle("ServerAvailable").getValue(getSimTime(), String.class).equals("1"))
				{               
					serverAvailableEntity = ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i);
					break;
				}
			}
			return serverAvailableEntity;		
		}
		
		public DisplayEntity getDisplayEntity(String name) {

			Entity ent = this.getJaamSimModel().getNamedEntity(name);
			DisplayEntity dispEnt = null;
			
			try {
				dispEnt = ((DisplayEntity)ent);
			} catch (ClassCastException exception) {
				System.out.print(name + " " + exception);
			}
			return dispEnt;
		}

		public double getInputValue(String name) {

			Entity inputvalueEnt = this.getJaamSimModel().getNamedEntity(name);
			double inputvalue = ((InputValue)inputvalueEnt).getNextSample(getSimTime());
			return inputvalue;
		}

		public double  getDistributionValue(String name) {

			Entity distributionEnt = this.getJaamSimModel().getNamedEntity(name);
			double distributionvalue = ((Distribution)distributionEnt).getNextSample(getSimTime());
			return distributionvalue;
		}

		public double getSimulationRunIndex(int index) {
			double indexvalue = this.getJaamSimModel().getRunIndexList().get(index);
			return indexvalue;
		}

		public double getTimeSeriesValue(String name) {

			Entity timeseriesEnt = getDisplayEntity(name);
			double timeseriesvalue = ((TimeSeries)timeseriesEnt).getPresentValue(getSimTime());
			return timeseriesvalue;
		}

		public double getNextChange(String name) {

			Entity timeseriesEnt = getDisplayEntity(name);
			double nextChange = ((TimeSeries)timeseriesEnt).getNextEventTime(getSimTime());
			return nextChange;
		}

		public void addUtilisation(DisplayEntity server) {

			String serverName = server.getName();

			double initializationDuration = this.getJaamSimModel().getSimulation().getInitializationDuration(getSimTime());

			// Set lastWorkingTime to zero
			if(getSimTime()>=initializationDuration && !utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				utilisationTimes.put("lastWorkingTime"+serverName,0.0); 

			}

			// Set lastWorkinTime to the total time the entity was working before during the initialization period
			else if (getSimTime()<initializationDuration  && !utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				double lastWorkingTime = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working");		
				utilisationTimes.put("lastWorkingTime"+serverName,lastWorkingTime); 

			}

			// Replace lastWorkingTime to the total time the entity was working before during the initialization period
			else if (getSimTime()<initializationDuration && utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				double lastWorkingTime = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working");		
				utilisationTimes.replace("lastWorkingTime"+serverName,lastWorkingTime); 
			}

			// Calculate the utilisation of the entity after the initialization period and place in utilisationTimes
			if (getSimTime()>=initializationDuration) {

				double runduration = this.getJaamSimModel().getSimulation().getRunDuration();
				double working = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working")-utilisationTimes.get("lastWorkingTime"+serverName);
				double utilisation = working/runduration;

				if (!utilisationTimes.containsKey("Utilisation"+serverName)) {

					utilisationTimes.put("Utilisation"+serverName,utilisation);
				}

				else if (utilisationTimes.containsKey("Utilisation"+serverName)) {

					utilisationTimes.replace("Utilisation"+serverName,utilisation);
				}

			}
		}

		public void sendActivitySignalToList(DisplayEntity active, DisplayEntity passive, String state) {

			if (((HCCMControlActivity)passive).EndActivitySignalList.getValue() != null) {
				for (HCCMController controller : ((HCCMControlActivity)passive).EndActivitySignalList.getValue()) {
					((HCCMController)controller).Controller(active, passive, state);
				}
			}
		}

		public void sendActivitySignal(DisplayEntity controller,DisplayEntity active, DisplayEntity passive, String state) {

			((HCCMController)controller).Controller(active, passive, state);
		}


		@Output(name = "CustomOutput1",
				description = "Custom Output",
				unitType = DimensionlessUnit.class,
				reportable = true,
				sequence = 2)
		public double getCustomOutput1(double simTime) {
			double output = 1;
			return output;
		}
	}
