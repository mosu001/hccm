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

public class ControllerEHC_Rosters extends HCCMController {

	// Create needed entities
	DisplayEntity waitingroom = null;
	DisplayEntity treatmentroom1 = null;
	DisplayEntity treatmentroom2 = null;
	DisplayEntity triageroom = null;
	DisplayEntity testroom = null;
	DisplayEntity waitingroomleave = null;
	DisplayEntity walkuppatientleave = null;
	DisplayEntity scheduledpatientleave = null;
	DisplayEntity triagenurseleave = null;
	DisplayEntity testnurseleave = null;
	DisplayEntity doctorleave = null;
	DisplayEntity doctor1roster = null;
	DisplayEntity doctor2roster = null;
	DisplayEntity controllerehcroster = null;
	DisplayEntity treat1finished = null;
	DisplayEntity treat2finished = null;
	DisplayEntity doctor1utilization = null;
	DisplayEntity doctor2utilization = null;
	DisplayEntity waitingroomtotriage = null;
	DisplayEntity triagetowaitingroom = null;
	DisplayEntity waitingroomtotest = null;
	DisplayEntity testtowaitingroom = null;
	DisplayEntity waitingroomtotreat1 = null;
	DisplayEntity waitingroomtotreat2 = null;
	DisplayEntity arrivalgate = null;

	// Create needed variables
	Double waitingroomcapacity = null;
	Double simindex = null;
	Double changeshiftdoctor1 = null;
	Double changeshiftdoctor2 = null;
	int retrywaittime = 1;
	int daystart = 0; // (day start in seconds)
	int lengthofday = 32400; //(9 hour day in seconds)
	int wrapuptime = 5400; // time in seconds to finish existing treatments etc roughly
	double waitingroomopen = 1.0;

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
		waitingroomleave = getDisplayEntity("WaitingRoomFullLeaveEvent");
		walkuppatientleave = getDisplayEntity("WalkUpPatientLeaveEvent");
		scheduledpatientleave = getDisplayEntity("ScheduledPatientLeaveEvent");
		triagenurseleave = getDisplayEntity("TriageNurseLeave");
		testnurseleave = getDisplayEntity("TestNurseLeave");
		doctorleave = getDisplayEntity("DoctorLeaveEvent");
		doctor1roster = getDisplayEntity("Doctor1Roster");
		doctor2roster = getDisplayEntity("Doctor2Roster");
		treat1finished = getDisplayEntity("Treat1Finished");
		treat2finished = getDisplayEntity("Treat2Finished");
		doctor1utilization = getDisplayEntity("Doctor1Utilization");
		doctor2utilization = getDisplayEntity("Doctor2Utilization");
		waitingroomtotriage = getDisplayEntity("WaitingRoomToTriage");
		triagetowaitingroom = getDisplayEntity("TriageToWaitingRoom");
		waitingroomtotest = getDisplayEntity("WaitingRoomToTest");
		testtowaitingroom = getDisplayEntity("TestToWaitingRoom");
		waitingroomtotreat1 = getDisplayEntity("WaitingRoomToTreat1");
		waitingroomtotreat2 = getDisplayEntity("WaitingRoomToTreat2");
		arrivalgate = getDisplayEntity("ArrivalGate");
		controllerehcroster = this;

		// Get needed variables
		simindex = getSimulationRunIndex(0);
		setWaitingRoomOpen(0.0);
		
		// Time when Doctor1 Shift changes, send signal to controller
		//changeshiftdoctor1 = getNextChange("Doctor1Roster");
		//this.scheduleProcess(changeshiftdoctor1, 5, "sendActivitySignal",controllerehcroster, doctor1roster, doctor1roster, "ChangeState");

		// Time when Doctor2 Shift changes, send signal to controller
		//changeshiftdoctor2 = getNextChange("Doctor2Roster");
		//this.scheduleProcess(changeshiftdoctor2, 5, "sendActivitySignal",controllerehcroster, doctor2roster, doctor2roster, "ChangeState");
		
		// Switch doctors
		this.scheduleProcess(86400, 5, "sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "DoctorRosterSwap");
		
		
		
		// End of day (uncomment to implement)
		//this.scheduleProcess(daystart + lengthofday - wrapuptime, 5, "sendActivitySignal", controllerehcroster, (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("AppointmentDoctor"), (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")), "WrapUpDay");
		//this.scheduleProcess(daystart + lengthofday, 5, "sendActivitySignal", controllerehcroster, (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("AppointmentDoctor"), (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")), "EndDay");
		
		// End of run 192h (168+24h setup), currently using wrap up day
		// Basic opening and closing times to test doctor switching (8am-6pm+ (ie. runs over time until all patients in waiting room have been processed))
		this.scheduleProcess(28800, 5, "sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "StartDay");
		//this.scheduleProcess(64800 - wrapuptime - 5, 5, "sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "WrapUpDay");
		//this.scheduleProcess(64800 - 5, 5, "sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "EndDay");		
		
		// Clear data Maps (utilisationTimes for this example)
		utilisationTimes.clear();

	}
	
	@Override
	public void Controller(DisplayEntity activeEntity, DisplayEntity activity, String state){
		//System.out.println(activeEntity.toString() + " " + activity.toString() + " " + state);

		// WalkUp Patient start Activity at WaitingRoom
		if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			
			// Set the appropriate state (since the waiting room handles multiple purposes)
			if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.001 && ((HCCMActiveEntity)walkuppatient).getState().toString() != "WaitForTriage") {
				((HCCMActiveEntity)walkuppatient).setPresentState("WaitForTriage");
			}
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && ((HCCMActiveEntity)walkuppatient).getState().toString() != "WaitForTest") {
				((HCCMActiveEntity)walkuppatient).setPresentState("WaitForTest");
			}
			else if (((HCCMActiveEntity)walkuppatient).getState().toString() != "WaitForTreat") {
				((HCCMActiveEntity)walkuppatient).setPresentState("WaitForTreat");
			}
			
			
			// If waiting room is full
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) >= ((HCCMControlActivity)waitingroom).getMaxQueueLength()) {
				moveEntFromTo(walkuppatient,waitingroom,waitingroomleave);
			}
			// Else if not triaged and Triage nurse available
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && serverAvailable("TriageNurse",triageroom) ) {
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
			// Else if triaged but not tested and needs test and Test nurse available
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && serverAvailable("TestNurse",testroom)) {
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
			// Finally, if ready for treatment (ie. triaged and either test completed or didnt need test)
			else if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (serverAvailable("Doctor",treatmentroom1)) && (Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 || Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01)) {
				sendActivitySignalToList(walkuppatient, waitingroom, "EndActivity");
			}
		}
		
		// WalkUp Patient ends Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "WaitingRoom", "EndActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			
			// Patient needs to be triaged, Triage Nurse is available, WalkUp Patient ends Activity WaitingRoom
			if (Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.01 && serverAvailable("TriageNurse",triageroom)) { 
				makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("TriageNurse"));
				moveEntFromTo(walkuppatient,waitingroom,waitingroomtotriage);
			}
			
			// Patient needs a test, Test Nurse is available, WalkUp Patient ends Activity WaitingRoom
			else if (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.001 && serverAvailable("TestNurse", testroom)) {
				makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TestRoom")).getFirstForMatch("TestNurse"));
				moveEntFromTo(walkuppatient,waitingroom,waitingroomtotest);
			}
			
			// WalkUp Doctor is available, patient has been triaged and either doesn't need a test or has completed it
			else if ((serverAvailable("Doctor",treatmentroom1)) && Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01)) {
				makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("Doctor"));
				moveEntFromTo(walkuppatient, waitingroom, waitingroomtotreat1);
			}

			// Appointment Doctor is available, patient has been triaged and either doesn't need a test or has completed it
			else if ((serverAvailable("Doctor",treatmentroom2) && Math.abs(walkuppatient.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(walkuppatient.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(walkuppatient.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01))) {
				makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor"));
				moveEntFromTo(walkuppatient,waitingroom,waitingroomtotreat2);
			}

		}
		
		// WalkUp Patient starts Activity at TriageRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TriageRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity triagenurse = ((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("TriageNurse");
			makeServerUnavailable(triagenurse);
			
			((HCCMActiveEntity)walkuppatient).setPresentState("Triage");
			((HCCMActiveEntity)triagenurse).setPresentState("Working");
			
			
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
			// Move patient back to waiting room and update nurse states
			ExpResult r = ExpResult.makeNumResult(1.0, DimensionlessUnit.class);
			((HCCMControlActivity)getDisplayEntity("TriageRoom")).getFirstForMatch("WalkUpPatient").setAttribute("hasBeenTriaged", null, r);
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,triageroom,triagetowaitingroom);
			makeServerAvailable((DisplayEntity)((HCCMControlActivity)triageroom).getFirstForMatch("TriageNurse"));
			DisplayEntity triagenurse = getServerAvailable("TriageNurse",triageroom);
			((HCCMActiveEntity)triagenurse).setPresentState("Idle");
			
			// Check waiting room for waiting patient, grab the one that has been waiting longest
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) > 0) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("WalkUpPatient")) {
						if (Math.abs(ent.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0)) < 0.01) {
							moveEntFromTo(ent, waitingroom, waitingroomtotriage);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)triageroom).getFirstForMatch("TriageNurse"));
							break;
						}
					}
				}
			}
		}		
		
		// WalkUp Patient starts Activity at TestRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TestRoom", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity testnurse = ((HCCMControlActivity)getDisplayEntity("TestRoom")).getFirstForMatch("TestNurse");
			makeServerUnavailable(testnurse);
			
			((HCCMActiveEntity)walkuppatient).setPresentState("Test");
			((HCCMActiveEntity)testnurse).setPresentState("Working");
			
			if (simindex == 1 || simindex == 2) {
				double testtime = 10*60;
				startScheduledActvitity(walkuppatient, testnurse, testroom, testtime);	
			}
			else if (simindex == 3 || simindex == 4) {
				//double testtime = getDistributionValue("TestDist");
				double testtime = 10*60;
				startScheduledActvitity(walkuppatient, testnurse, testroom, testtime);	
			}			
		}

		// WalkUp Patient ends Activity at TestRoom
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TestRoom", "EndActivity")) {
			// Move patient back to waiting room and update nurse states
			DisplayEntity walkuppatient = activeEntity;
			ExpResult r = ExpResult.makeNumResult(1.0, DimensionlessUnit.class);
			walkuppatient.setAttribute("hasBeenTested", null, r);
			moveEntFromTo(walkuppatient,testroom,testtowaitingroom);
			makeServerAvailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TestRoom")).getFirstForMatch("TestNurse"));
			DisplayEntity testnurse = getServerAvailable("TestNurse",testroom);
			((HCCMActiveEntity)testnurse).setPresentState("Idle");

			// Check waiting room for waiting patient, grab the one that has been waiting longest
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) > 0) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("WalkUpPatient")) {
						if (Math.abs(ent.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(ent.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && Math.abs(ent.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0)) < 0.01) {
							moveEntFromTo(ent, waitingroom, waitingroomtotest);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)testroom).getFirstForMatch("TestNurse"));
							break;
						}
					}
				}
			}
		}
		
		// WalkUp Patient starts Activity at TreatmentRoom1
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom1", "StartActivity")) {
			DisplayEntity walkuppatient = activeEntity;
			DisplayEntity walkupdoctor = ((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("Doctor");
			makeServerUnavailable(walkupdoctor);

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");
			((HCCMActiveEntity)walkupdoctor).setPresentState("Working");

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
			DisplayEntity appointmentdoctor = ((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor");
			makeServerUnavailable(appointmentdoctor);

			((HCCMActiveEntity)walkuppatient).setPresentState("Treat");
			((HCCMActiveEntity)appointmentdoctor).setPresentState("Working");

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
			// Move patient on and update doctor states
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,treatmentroom1,treat1finished);
			makeServerAvailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("Doctor"));
			DisplayEntity walkupdoctor = (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom1")).getFirstForMatch("Doctor");
			((HCCMActiveEntity)walkupdoctor).setPresentState("Idle");
			
			// Check waiting room for waiting patient, grab the one that has been waiting longest
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) > 0) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("WalkUpPatient")) {
						if (Math.abs(ent.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(ent.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(ent.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01)) {
							moveEntFromTo(ent, waitingroom, waitingroomtotreat1);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)treatmentroom1).getFirstForMatch("Doctor"));
							break;
						}
					}
				}
			}
		}

		// WalkUp Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "WalkUpPatient", "TreatmentRoom2", "EndActivity")) {
			// Move patient on and update doctor states
			DisplayEntity walkuppatient = activeEntity;
			moveEntFromTo(walkuppatient,treatmentroom2,treat2finished);
			makeServerAvailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor"));
			DisplayEntity appointmentdoctor = (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor");
			((HCCMActiveEntity)appointmentdoctor).setPresentState("Idle");

			// Check waiting room for scheduled patient where the current time is >= their appointment time
			boolean moved = false;
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) > 0) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("ScheduledPatient")) {
						if (ent.getOutputHandle("AppointmentTime").getValueAsDouble(getSimTime(), 0.0)*3600 > getSimTime())
							moveEntFromTo(ent, waitingroom, waitingroomtotreat2);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor"));
							moved = true;
							break;
					}
				}
			}
			// If no scheduled patients, check for a walkup patient ready to be treated
			if (moved == false) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("WalkUpPatient")) {
						if (Math.abs(ent.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(ent.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(ent.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01)) {
							moveEntFromTo(ent, waitingroom, waitingroomtotreat2);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor"));
							break;
						}
					}
				}
			}
		}
		
		// Scheduled Patient start Activity at WaitingRoom
		else if(happens(activeEntity, activity, state, "ScheduledPatient", "WaitingRoom", "StartActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			// Set State
			if (((HCCMActiveEntity)scheduledpatient).getState().toString() != "WaitForTreat") {
				((HCCMActiveEntity)scheduledpatient).setPresentState("WaitForTreat");
			}

			// Appointment Doctor is available, Scheduled Patient ends Activity WaitingRoom
			if  ((serverAvailable("Doctor", treatmentroom2)) && scheduledpatient.getOutputHandle("AppointmentTime").getValueAsDouble(getSimTime(), 0.0)*60 >= getSimTime()) { 
				sendActivitySignalToList(scheduledpatient, waitingroom, "EndActivity");
			}			
		}

		// Scheduled Patient end Activity at WaitingRoom
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "WaitingRoom", "EndActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			makeServerUnavailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor"));
			moveEntFromTo(scheduledpatient,waitingroom,waitingroomtotreat2);
		}

		// Scheduled Patient starts Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "TreatmentRoom2", "StartActivity")) {
			DisplayEntity scheduledpatient = activeEntity;
			DisplayEntity appointmentdoctor = ((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor");
			makeServerUnavailable(appointmentdoctor);

			((HCCMActiveEntity)scheduledpatient).setPresentState("Treat");
			((HCCMActiveEntity)appointmentdoctor).setPresentState("Working");

			if (simindex == 1 || simindex == 2) {
				double treatmenttime = 15*60;
				startScheduledActvitity (scheduledpatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}
			else if (simindex == 3 || simindex == 4) {
				double treatmenttime = getDistributionValue("AppointmentTreatDist");
				startScheduledActvitity (scheduledpatient, appointmentdoctor, treatmentroom2, treatmenttime);
			}

		}

		// Scheduled Patient ends Activity at TreatmentRoom2
		else if (happens(activeEntity, activity, state, "ScheduledPatient", "TreatmentRoom2", "EndActivity")) {
			// Move patient on and update doctor states
			DisplayEntity scheduledpatient = activeEntity;
			moveEntFromTo(scheduledpatient,treatmentroom2,treat2finished);
			makeServerAvailable((DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor"));
			DisplayEntity appointmendoctor = (DisplayEntity)((HCCMControlActivity)getDisplayEntity("TreatmentRoom2")).getFirstForMatch("Doctor");
			((HCCMActiveEntity)appointmendoctor).setPresentState("Idle");
			
			// Check waiting room for scheduled patient where the current time is >= their appointment time
			boolean moved = false;
			if (((HCCMControlActivity)waitingroom).getQueueLength(getSimTime()) > 0) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("ScheduledPatient")) {
						if (ent.getOutputHandle("AppointmentTime").getValueAsDouble(getSimTime(), 0.0)*3600 > getSimTime())
							moveEntFromTo(ent, waitingroom, waitingroomtotreat2);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor"));
							moved = true;
							break;
					}
				}
			}
			// If no scheduled patients, check for a walkup patient ready to be treated
			if (moved == false) {
				for (DisplayEntity ent : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
					if (ent.getOutputHandle("Match").getValue(getSimTime(), String.class).equals("WalkUpPatient")) {
						if (Math.abs(ent.getOutputHandle("hasBeenTriaged").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01 && (Math.abs(ent.getOutputHandle("NeedsTest").getValueAsDouble(getSimTime(), 1.0)) < 0.01 || Math.abs(ent.getOutputHandle("hasBeenTested").getValueAsDouble(getSimTime(), 1.0) - 1.0) < 0.01)) {
							moveEntFromTo(ent, waitingroom, waitingroomtotreat2);
							makeServerUnavailable((DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor"));
							break;
						}
					}
				}
			}

		}		

		// Shift swap every 24 hours
		else if (happens(activeEntity, activity, state, "WaitingRoom", "WaitingRoom", "DoctorRosterSwap")) {
			// Swap the positions of the two doctors
			DisplayEntity walkupdoctor = (DisplayEntity)((HCCMControlActivity)treatmentroom1).getFirstForMatch("Doctor");
			DisplayEntity appointmentdoctor = (DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor");
			moveEntFromTo(walkupdoctor, treatmentroom1, treatmentroom2);
			moveEntFromTo(appointmentdoctor, treatmentroom2, treatmentroom1);
			
			this.scheduleProcess(this.getSimTime() + 86400, 5, "sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "DoctorRosterSwap");
			
		}
		/*
		// Shift of WalkUpDoctor Ends
		else if (happens(activeEntity, activity, state, "Doctor1Roster", "Doctor1Roster", "ChangeState")) {
			double WalkUpDoctorShift = getTimeSeriesValue("Doctor1Roster");
			if (WalkUpDoctorShift == 0) {
				while (serverAvailable("Doctor",treatmentroom1)) {
					DisplayEntity walkupdoctor = (DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor");
					moveEntFromTo(walkupdoctor,treatmentroom1, doctorleave);
				}
			}
			changeshiftdoctor1 = getNextChange("Doctor1Roster")-getSimTime();
			// Schedule next change
			this.scheduleProcess(changeshiftdoctor1, 5, "sendActivitySignal",controllerehcroster, doctor1roster, doctor1roster, "ChangeState");
		}
		
		// Shift of AppointmentDoctor Ends
		else if (happens(activeEntity, activity, state, "Doctor2Roster", "Doctor2Roster", "ChangeState")) {
			double AppointmentDoctorShift = getTimeSeriesValue("Doctor2Roster");
			if (AppointmentDoctorShift == 0) {
				while (serverAvailable("AppointmentDoctor",treatmentroom2) || serverAvailable("AppointmentDoctor",treatmentroom2)) {
					DisplayEntity appointmentdoctor = (DisplayEntity)((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor");
					moveEntFromTo(appointmentdoctor,treatmentroom2, doctorleave);
				}
			}
			changeshiftdoctor2 = getNextChange("Doctor2Roster")-getSimTime();// simTime;
			// Schedule next change
			this.scheduleProcess(changeshiftdoctor2, 5, "sendActivitySignal",controllerehcroster, doctor2roster, doctor2roster, "ChangeState");
		}		
		*/
		// Wrap up day
		else if (happens(activeEntity, activity, state, "WaitingRoom", "WaitingRoom", "WrapUpDay")) {
			// Move all patients in waiting room who haven't started triage to leave
			setWaitingRoomOpen(0.0);
			for (DisplayEntity e : ((HCCMControlActivity)waitingroom).getQueueList(getSimTime())) {
				if (((HCCMActiveEntity)e).getState().toString() == "WaitForTriage") {
					moveEntFromTo(((HCCMControlActivity)waitingroom).getFirst(), waitingroom, waitingroomleave);
				}
			}			
		}
		
		// End of Day
		else if (happens(activeEntity, activity, state, "WaitingRoom", "WaitingRoom", "EndDay")) {
			//moveEntFromTo(((HCCMControlActivity)triageroom).getFirstForMatch("TriageNurse"), triageroom, triagenurseleave);
			//moveEntFromTo(((HCCMControlActivity)testroom).getFirstForMatch("TestNurse"), testroom, testnurseleave);
			//moveEntFromTo(((HCCMControlActivity)treatmentroom1).getFirstForMatch("Doctor"), treatmentroom1, doctor1utilization);
			//moveEntFromTo(((HCCMControlActivity)treatmentroom2).getFirstForMatch("Doctor"), treatmentroom2, doctor2utilization);
			
			this.scheduleProcess(50400,5,"sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "StartDay");
		}
		
		// Start Day
		else if (happens(activeEntity, activity, state, "WaitingRoom", "WaitingRoom", "StartDay")) {
			setWaitingRoomOpen(1.0);
			this.scheduleProcess(36000-wrapuptime-5,5,"sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "WrapUpDay");
			this.scheduleProcess(36000,5,"sendActivitySignal", controllerehcroster, waitingroom, waitingroom, "EndDay");
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

		/*
		@Output(name = "CustomOutput1",
				description = "Custom Output",
				unitType = DimensionlessUnit.class,
				reportable = true,
				sequence = 2)
		public double getCustomOutput1(double simTime) {
			double output = 1;
			return output;
		}
		*/
		@Output(name = "WaitingRoomOpen",
				description = "1 for open, 0 for closed",
				unitType = DimensionlessUnit.class)
		public double getWaitingRoomOpen(double simTime) {
			return this.waitingroomopen;
		}
		public void setWaitingRoomOpen(double v) {
			this.waitingroomopen = v;
		}
	}
