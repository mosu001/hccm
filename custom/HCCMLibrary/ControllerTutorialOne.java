package HCCMLibrary;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.basicsim.Entity;

public class ControllerTutorialOne {

	public static void Controller(DisplayEntity active, DisplayEntity passive, String state) {

		
		//  Create variables
		double waitingroomcapacity = Double.POSITIVE_INFINITY;	
		Entity waitingroom = active.getJaamSimModel().getNamedEntity("WaitingRoom");
		Entity doctoridle = active.getJaamSimModel().getNamedEntity("DoctorIdle");
		
		//Entity treatmentroom = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
		//System.out.println(((HCCMEntityDelayControlledOutput)treatmentroom).getNumberInProgress());
		
		// Patient arrives at Clinic
		if (active.getName().startsWith("Patient") && passive.getName().equals("WaitingRoom") && state.equals("Event")) {

			//System.out.println("Patient enters WaitingRoom");

			// WaitingRoom is full, send Patient to Outside
			if (((HCCMQueue)waitingroom).getNumberInProgress() > waitingroomcapacity) {
				
				//System.out.println("Patient leaves Clinic");
				
				((HCCMQueue)passive).removeEntity(active);
				Entity toGo = active.getJaamSimModel().getNamedEntity("PatientLeave");
				((Linkable)toGo).addEntity(active);

			}	

			// Doctor and TreatmentRoom are available, send Patient and Doctor to TreatmentRoom
			else if (((HCCMQueue)doctoridle).getNumberInProgress() > 0) { 

				//((HCCMEntityDelayControlledOutput)treatmentroom).getNumberInProgress() == 0 &&
				
				//System.out.println("Patient enters TreatmentRoom");
				
				((HCCMQueue)passive).removeEntity(active);
				DisplayEntity firstdoctor = ((HCCMQueue)doctoridle).getFirst();
				((HCCMQueue)doctoridle).removeFirst();
				((EntityContainer)firstdoctor).addEntity(active);
    			Entity toGo = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
				((Linkable)toGo).addEntity(firstdoctor);

			}

		}

		// Doctor becomes Idle
		else if (active.getName().startsWith("Doctor") && passive.getName().equals("TreatmentRoom") && state.equals("EndActivity")) {

			//System.out.println("Doctor becomes Idle");

			// Patient is waiting
			if (((HCCMQueue)waitingroom).getNumberInProgress() > 0  ) {

				//System.out.println("Patient enters TreatmentRoom");
				
				// Set Patient in Doctor(Container)
				DisplayEntity patientindoctor = ((EntityContainer)active).removeEntity(null);
    			Entity toGo = active.getJaamSimModel().getNamedEntity("PatientFinished");
				((Linkable)toGo).addEntity(patientindoctor);			
				
				DisplayEntity firstpatient = ((HCCMQueue)waitingroom).getFirst();
				((HCCMQueue)waitingroom).removeFirst();
				((EntityContainer)active).addEntity(firstpatient);
    			Entity toGo2 = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
				((Linkable)toGo2).addEntity(active);
				

			}
			
			// No Patient is waiting
			else if (((HCCMQueue)waitingroom).getNumberInProgress() == 0  ) {
				
				//System.out.println("Doctor waits for new Patient");
						
				DisplayEntity patientindoctor = ((EntityContainer)active).removeEntity(null);
    			
				Entity toGo = active.getJaamSimModel().getNamedEntity("PatientFinished");
				((Linkable)toGo).addEntity(patientindoctor);			
    			Entity toGo2 = active.getJaamSimModel().getNamedEntity("DoctorIdle");
				((Linkable)toGo2).addEntity(active);
			}
		}

		// Doctor becomes Idle
		else if (active.getName().startsWith("Doctor") && passive.getName().equals("DoctorIdle") && state.equals("Event")) {
	
			// Patient is waiting
			if (((HCCMQueue)waitingroom).getNumberInProgress() > 0  ) {

				//System.out.println("Patient enters TreatmentRoom");
				
				DisplayEntity firstpatient = ((HCCMQueue)waitingroom).getFirst();
				((HCCMQueue)waitingroom).removeFirst();
				DisplayEntity firstdoctor = ((HCCMQueue)doctoridle).getFirst();
				((HCCMQueue)doctoridle).removeFirst();
				((EntityContainer)firstdoctor).addEntity(firstpatient);
    			Entity toGo = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
				((Linkable)toGo).addEntity(firstdoctor);
				

			}
		}
		}

}
