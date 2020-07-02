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

public class ControllerTutorialOne extends HCCMController{

	// Create needed entities
	DisplayEntity waitingroom =  null;
	DisplayEntity patientleave = null; 
	DisplayEntity treatmentroom = null;
	DisplayEntity patientfinished = null;
	DisplayEntity doctorleave = null;
	DisplayEntity doctor1roster = null;
	DisplayEntity controllertutorialone = null;

	// Create needed variables
	Double waitingroomcapacity = null;
	Double simindex = null;
	Double changeshift = null;
	
	// Create needed data Maps
	Map<String, Double> utilisationTimes = new HashMap<String, Double>();
	
	@Override
	public void earlyInit() {
		super.earlyInit();

		// Get needed entities
		waitingroom = getDisplayEntity("WaitingRoom");
		patientleave = getDisplayEntity("PatientLeave"); 
		treatmentroom = getDisplayEntity("TreatmentRoom");
		patientfinished = getDisplayEntity("PatientFinished");
		doctorleave = getDisplayEntity("DoctorLeave"); 
		doctor1roster = getDisplayEntity("Doctor1Roster");
		controllertutorialone = getDisplayEntity("ControllerTutorialOne1");

		// Get needed variables
		waitingroomcapacity = getInputValue("WaitingRoomCapacity");
		simindex = getSimulationRunIndex(0);

		// Time when DoctorShift changes, send signal to controller
		changeshift = getNextChange("Doctor1Roster");
		this.scheduleProcess(changeshift, 5, "sendActivitySignal",controllertutorialone, doctor1roster, doctor1roster, "ChangeState");
	
		// Clear data Maps
		utilisationTimes.clear();
	}


	@Override
	public void Controller(DisplayEntity activeEntity, DisplayEntity activity, String state){

		// Patient start Activity at WaitingRoom
		if (happens(activeEntity, activity, state, "Patient", "WaitingRoom", "StartActivity")) {

			DisplayEntity patient = activeEntity;
			((HCCMActiveEntity)patient).setPresentState("Wait");

			// WaitingRoom was full, send Patient to Outside
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() >= waitingroomcapacity) {
				moveEntFromTo(patient,waitingroom,patientleave);
			}	

			// Doctor is available, Patient ends Activity WaitingRoom
			else if (serverAvailable("Doctor",treatmentroom)) { 
				sendActivitySignalToList(patient, waitingroom, "EndActivity");
			}
		}

		// Patient ends Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "Patient", "WaitingRoom", "EndActivity")) {

			DisplayEntity patient = activeEntity;

			moveEntFromTo(patient,waitingroom,treatmentroom);
		}


		// Patient starts Activity at TreatmentRoom
		else if (happens(activeEntity, activity, state, "Patient", "TreatmentRoom", "StartActivity")) {

			DisplayEntity patient = activeEntity;
			DisplayEntity doctor = getServerAvailable("Doctor",treatmentroom);

			((HCCMActiveEntity)patient).setPresentState("Treat");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity(patient, doctor, treatmentroom, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("TreatmentDist");
				startScheduledActvitity(patient, doctor, treatmentroom, treatmenttime);
			}

		}

		// Patient ends Activity at TreatmentRoom
		else if (happens(activeEntity, activity, state, "Patient", "TreatmentRoom", "EndActivity")) {

			DisplayEntity patient = activeEntity;
			moveEntFromTo(patient,treatmentroom,patientfinished);

		}

		// Doctor starts Activity at TreatmentRoom
		else if (happens(activeEntity, activity, state, "Doctor", "TreatmentRoom", "StartActivity")) {

			DisplayEntity doctor = activeEntity;
			makeServerAvailable(doctor);


			// Patient is waiting, Patient ends Activity at WaitingRoom
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() > 0  ) {

				DisplayEntity firstpatient = ((HCCMControlActivity)waitingroom).getFirstForMatch("Patient");

				sendActivitySignalToList(firstpatient, waitingroom, "EndActivity");
			}
		}

		// Doctor ends Activity at TreatmentRoom
		else if (happens(activeEntity, activity, state, "Doctor", "TreatmentRoom", "EndActivity")) {

			DisplayEntity doctor = activeEntity;
			double doctor1shift = getTimeSeriesValue("Doctor1Roster");
			
			// Add Statistics for utilisation
			addUtilisation(doctor);
			
			// No Patient is waiting and Doctor_Shift == 0
			if (((HCCMControlActivity)waitingroom).getNumberInProgress() == 0 && doctor1shift == 0 ) {

				moveEntFromTo(doctor,treatmentroom, doctorleave);
			}

			else {
				moveEntFromTo(doctor,treatmentroom,treatmentroom);
			}
		}

		// Value of Doctor_Shift changes
		else if (happens(activeEntity, activity, state, "Doctor1Roster", "Doctor1Roster", "ChangeState")) {

			double DoctorShift = getTimeSeriesValue("Doctor1Roster");
			
			// Doctor has to end shift
			if (DoctorShift == 0) {

				// Remove the doctor which is Idle
				while (serverAvailable("Doctor",treatmentroom)) {
					DisplayEntity doctor = getServerAvailable("Doctor", treatmentroom);
					moveEntFromTo(doctor,treatmentroom, doctorleave);
				}
			}

			// Schedule next change
			changeshift = getNextChange("Doctor1Roster")-getSimTime();
			this.scheduleProcess(changeshift, 5, "sendActivitySignal",controllertutorialone, doctor1roster, doctor1roster, "ChangeState");
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
	
	@Output(name = "UtilisationDoctor1",
			description = "The Utilisation of Doctor1",
			unitType = DimensionlessUnit.class,
			reportable = true,
			sequence = 2)
	public double getUtilisationDoctor1(double simTime) {

		double utilisation = utilisationTimes.get("UtilisationDoctor1");
		return utilisation;
	}


}
