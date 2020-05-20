package HCCMLibrary.controllers;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProbabilityDistributions.Distribution;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.TimeSeries;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jaamsim.BasicObjects.*;
import HCCMLibrary.controlactivity.HCCMControlActivity;
import HCCMLibrary.entities.HCCMActiveEntity;

public class ControllerTutorialTwo extends HCCMController{

	// Create needed entities
	DisplayEntity waitingroom =  null;
	DisplayEntity patientleave = null; 
	DisplayEntity treatmentroom1 = null;
	DisplayEntity treatmentroom2 = null;
	DisplayEntity walkuppatientfinished = null;
	DisplayEntity scheduledpatientfinished = null;
	DisplayEntity doctorleave = null; 
	DisplayEntity walkupdoctorroster = null;
	DisplayEntity appointmentdoctorroster = null;
	DisplayEntity controllertutorialtwo = null;

	// Create needed variables
	Double waitingroomcapacity = null;
	Double simindex = null;
	Double changeshiftwalkupdoctor = null;
	Double changeshiftappointmentdoctor = null;

	// Create needed data Maps
	Map<String, Double> utilisationTimes = new HashMap<String, Double>();

	@Override
	public void earlyInit() {
		super.earlyInit();

		// Get needed entities
		waitingroom = getDisplayEntity("WaitingRoom");
		patientleave = getDisplayEntity("PatientLeave"); 
		treatmentroom1 = getDisplayEntity("TreatmentRoom1");
		treatmentroom2 = getDisplayEntity("TreatmentRoom2");
		walkuppatientfinished = getDisplayEntity("WalkUpPatientFinished");
		scheduledpatientfinished = getDisplayEntity("ScheduledPatientFinished");
		doctorleave = getDisplayEntity("DoctorLeave"); 
		walkupdoctorroster = getDisplayEntity("WalkUpDoctorRoster");
		appointmentdoctorroster = getDisplayEntity("AppointmentDoctorRoster");
		controllertutorialtwo = getDisplayEntity("ControllerTutorialTwo1");

		// Get needed variables
		waitingroomcapacity = getInputValue("WaitingRoomCapacity");
		simindex = getSimulationRunIndex(0);

		// Time when WalkUpDoctorShift changes, send signal to controller
		changeshiftwalkupdoctor = getNextChange("WalkUpDoctorRoster");
		this.scheduleProcess(changeshiftwalkupdoctor, 5, "sendActivitySignal",controllertutorialtwo, walkupdoctorroster, walkupdoctorroster, "ChangeState");

		// Time when AppointmentDoctorShift changes, send signal to controller
		changeshiftappointmentdoctor = getNextChange("AppointmentDoctorRoster");
		this.scheduleProcess(changeshiftappointmentdoctor, 5, "sendActivitySignal",controllertutorialtwo, appointmentdoctorroster, appointmentdoctorroster, "ChangeState");

	}

	@Override
	public void Controller(DisplayEntity activeEntity, DisplayEntity activity, String state){

		// WalkUp Patient start Activity at WaitingRoom
		if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			((HCCMActiveEntity)walkuppatient).setPresentState("Wait");

			// WaitingRoom is full, send Patient to Outside
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() >= waitingroomcapacity) {
				moveEntFromTo(walkuppatient,waitingroom,patientleave);
			}	

			// WalkUp Doctor or AppointmentDoctor is available, WalkUp Patient ends Activity WaitingRoom
			else if (serverAvailable("WalkUpDoctor",treatmentroom1) || serverAvailable("AppointmentDoctor",treatmentroom2)) { 
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
		}

		// WalkUp Patient ends Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;

			// WalkUp Doctor is available, WalkUp Patient ends Activity WaitingRoom
			if (serverAvailable("WalkUpDoctor",treatmentroom1)) { 
				moveEntFromTo(walkuppatient,waitingroom,treatmentroom1);
			}

			// Appointment Doctor is available, WalkUp Patient ends Activity WaitingRoom
			else if  (serverAvailable("AppointmentDoctor",treatmentroom2)) { 
				moveEntFromTo(walkuppatient,waitingroom,treatmentroom2);
			}
		}
		
		// WalkUp Patient starts Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom1", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity walkupdoctor = getServerAvailable("WalkUpDoctor",treatmentroom1);

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (walkuppatient, walkupdoctor, treatmentroom1, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("TreatmentDist");
				startScheduledActvitity (walkuppatient, walkupdoctor, treatmentroom1, treatmenttime);
			}

		}

		// WalkUp Patient starts Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom2", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity appointmentdoctor = getServerAvailable("AppointmentDoctor",treatmentroom2);

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (walkuppatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("TreatmentDist");
				startScheduledActvitity (walkuppatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}

		}
		
		// WalkUp Patient ends Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom1", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,treatmentroom1,walkuppatientfinished);
		}

		// WalkUp Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom2", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,treatmentroom2,walkuppatientfinished);
		}

		
		// Scheduled Patient start Activity at WaitingRoom
		else if(happens(activeEntity, activity, state, "ScheduledPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			((HCCMActiveEntity)scheduledpatient).setPresentState("Wait");

			// Appointment Doctor is available, Scheduled Patient ends Activity WaitingRoom
			if  (serverAvailable("AppointmentDoctor",treatmentroom2)) { 
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
				double treatmenttime = getDistributionValue("TreatmentDist");
				startScheduledActvitity (scheduledpatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}

		}

		// Scheduled Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "TreatmentRoom2", "EndActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			moveEntFromTo(scheduledpatient,treatmentroom2,scheduledpatientfinished);

		}

		
		// WalkUp Doctor starts Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpDoctor", "TreatmentRoom1", "StartActivity")) {
			DisplayEntity walkupdoctor = activeEntity;
			makeServerAvailable(walkupdoctor);

			// WalkUp Patient is waiting, WalkUp Patient ends Activity at WaitingRoom
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() > 0 && ((HCCMControlActivity)waitingroom).getMatchValues(getSimTime()).contains("WalkUpPatient")) {
				DisplayEntity firstwalkuppatient = ((HCCMControlActivity)waitingroom).getFirstForMatch("WalkUpPatient");
				sendActivitySignalToList(firstwalkuppatient, waitingroom, "EndActivity");
			}
		}
		
		// WalkUp Doctor ends Activity at TreatmentRoom1 
		else if (happens(activeEntity, activity, state, "WalkUpDoctor", "TreatmentRoom1", "EndActivity")) {
			DisplayEntity walkupdoctor = activeEntity;
			double walkupdoctorshift = getTimeSeriesValue("WalkUpDoctorRoster");

			addUtilisation(walkupdoctor);

			// No Patient is waiting and WalkUpDoctor_Shift == 0
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() == 0 && walkupdoctorshift == 0 ) {
				moveEntFromTo(walkupdoctor,treatmentroom1, doctorleave);
			}
			else {
				moveEntFromTo(walkupdoctor,treatmentroom1,treatmentroom1);
			}
		}

		
		// Appointment Doctor starts Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "AppointmentDoctor", "TreatmentRoom2", "StartActivity")) {
			DisplayEntity appointmentdoctor = activeEntity;
			makeServerAvailable(appointmentdoctor);

			// Scheduled Patient is waiting, Scheduled Patient ends Activity at Waitingroom
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() > 0 && ((HCCMControlActivity)waitingroom).getMatchValues(getSimTime()).contains("ScheduledPatient")) {
				DisplayEntity firstscheduledpatient = ((HCCMControlActivity)waitingroom).getFirstForMatch("ScheduledPatient");
				sendActivitySignalToList(firstscheduledpatient, waitingroom, "EndActivity");
			}
			// WalkUpPatient is waiting, WalkUpPatient ends Activity at Waitingroom
			else if (((HCCMControlActivity)waitingroom).getNumberInProgress() > 0 && ((HCCMControlActivity)waitingroom).getMatchValues(getSimTime()).contains("WalkUpPatient")) {
				DisplayEntity firstwalkuppatient = ((HCCMControlActivity)waitingroom).getFirstForMatch("WalkUpPatient");
				sendActivitySignalToList(firstwalkuppatient, waitingroom, "EndActivity");
			}
		}

		// Appointment Doctor ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "AppointmentDoctor", "TreatmentRoom2", "EndActivity")) {
			DisplayEntity appointmentdoctor = activeEntity;
			double appointmentdoctorshift = getTimeSeriesValue("AppointmentDoctorRoster");

			addUtilisation(appointmentdoctor);

			// No Patient is waiting and Appointment Doctor_Shift == 0
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() == 0 && appointmentdoctorshift == 0 ) {
				moveEntFromTo(appointmentdoctor,treatmentroom2, doctorleave);
			}
			else {
				moveEntFromTo(appointmentdoctor,treatmentroom2,treatmentroom2);
			}
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
			this.scheduleProcess(changeshiftwalkupdoctor, 5, "sendActivitySignal",controllertutorialtwo, walkupdoctorroster, walkupdoctorroster, "ChangeState");
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
			this.scheduleProcess(changeshiftappointmentdoctor, 5, "sendActivitySignal",controllertutorialtwo, appointmentdoctorroster, appointmentdoctorroster, "ChangeState");
		}

	}

	public void startScheduledActvitity (DisplayEntity customer, DisplayEntity server, DisplayEntity activity, double duration) {
		// Starts a scheduled activity for the given customer and server with a specified duration
		
		makeServerUnavailable(server);
		((HCCMActiveEntity)server).setPresentState("Working");

		this.scheduleProcess(duration, 5, "sendActivitySignalToList", customer, activity, "EndActivity");
		this.scheduleProcess(duration, 5, "sendActivitySignalToList", server, activity, "EndActivity");
	}

	public void moveEntFromTo(DisplayEntity Ent, DisplayEntity From ,DisplayEntity To) {
		// Moves an entity from one entity to another entity in the system
		
		((HCCMControlActivity)From).removeEntity(Ent);
		((Linkable)To).addEntity(Ent);	
	}
	
	public boolean happens(DisplayEntity active, DisplayEntity activity, String state, String ifactive, String ifactivity, String ifstate) {
		// True if the control signal is similar to the control trigger
		
		DisplayEntity ifactivityEnt = getDisplayEntity(ifactivity);
		boolean happen = (active.getName().startsWith(ifactive) && activity.equals(ifactivityEnt) && state.equals(ifstate));
		return happen;
	}

	public boolean serverAvailable(String server, DisplayEntity location) {
		// True if the given server is available in the given ControlActivity (location)
				
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
		// Changes the ServerAvailable attribute of the server to 1 and sets the state to Idle
				
		((HCCMActiveEntity)server).setPresentState("Idle");
		ExpResult eR = ExpResult.makeStringResult("1");
		((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);
	}

	public void makeServerUnavailable(DisplayEntity server) {
		// Changes the ServerAvailable attribute of the server to 0
		
		ExpResult eR = ExpResult.makeStringResult("0");
		((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);

	}

	public DisplayEntity getServerAvailable(String server, Entity location) {
		// Gets the server in the ControlActivity which is available for serving a customer
		
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
		// Gets the DisplayEntity of the model with the given name
		
		Entity ent = this.getJaamSimModel().getNamedEntity(name);
		DisplayEntity dispEnt = ((DisplayEntity)ent);
		return dispEnt;
	}

	public double getInputValue(String name) {
		// Gets the value of an InputValue entity with the given name
		
		Entity inputvalueEnt = this.getJaamSimModel().getNamedEntity(name);
		double inputvalue = ((InputValue)inputvalueEnt).getNextSample(getSimTime());
		return inputvalue;
	}

	public double  getDistributionValue(String name) {
		// Gets the value of a Probability Distribution entity with the given name
		
		Entity distributionEnt = this.getJaamSimModel().getNamedEntity(name);
		double distributionvalue = ((Distribution)distributionEnt).getNextSample(getSimTime());
		return distributionvalue;
	}

	public double getSimulationRunIndex(int index) {
		// Gets the value of the simulation RunIndex for the given index
		
		double indexvalue = this.getJaamSimModel().getRunIndexList().get(index);
		return indexvalue;
	}

	public double getTimeSeriesValue(String name) {
		// Gets the value of a TimeSeries entity with the given name
		
		Entity timeseriesEnt = getDisplayEntity(name);
		double timeseriesvalue = ((TimeSeries)timeseriesEnt).getPresentValue(getSimTime());
		return timeseriesvalue;
	}

	public double getNextChange(String name) {
		// Gets the simulation time moment when the value of TimeSeries with the given name changes
		
		Entity timeseriesEnt = getDisplayEntity(name);
		double nextChange = ((TimeSeries)timeseriesEnt).getNextEventTime(getSimTime());
		return nextChange;
	}

	public void addUtilisation(DisplayEntity server) {
		// Adds the next calculated utilisation of the given server
		
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

	public void sendActivitySignalToList(DisplayEntity active, DisplayEntity activity, String state) {
		// Sends a control signal to the controllers which are linked to this activity
		
		if (((HCCMControlActivity)activity).EndActivitySignalList.getValue() != null) {
			for (HCCMController controller : ((HCCMControlActivity)activity).EndActivitySignalList.getValue()) {
				((HCCMController)controller).Controller(active, activity, state);
			}
		}
	}

	public void sendActivitySignal(DisplayEntity controller,DisplayEntity active, DisplayEntity activity, String state) {
		// Sends a control signal to the given controller
		
		((HCCMController)controller).Controller(active, activity, state);
	}


	@Output(name = "UtilisationWalkUpDoctor1",
			description = "Utilisation of WalkUpDoctor1",
			unitType = DimensionlessUnit.class,
			reportable = true,
			sequence = 2)
	public double getUtilisationWalkUpDoctor(double simTime) {

		double utilisation = utilisationTimes.get("UtilisationWalkUpDoctor1");
		return utilisation;
	}

	@Output(name = "UtilisationAppointmentDoctor1",
			description = "Utilisation of AppointmentDoctor1",
			unitType = DimensionlessUnit.class,
			reportable = true,
			sequence = 2)
	public double getUtilisationAppointmentDoctor(double simTime) {

		double utilisation = utilisationTimes.get("UtilisationAppointmentDoctor1");
		return utilisation;
	}

}