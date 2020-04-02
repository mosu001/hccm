package HCCMLibrary;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.ProcessFlow.Queue;
import com.jaamsim.basicsim.Entity;


public class ControllerSSQ {

	public static void Controller(DisplayEntity active, DisplayEntity passive) {
		
		Entity treatmentroom = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
		double waitingroomcapacity = 5;	
		Entity waitingroom = active.getJaamSimModel().getNamedEntity("WaitingRoom");

		if (active.getName().startsWith("Patient") && passive.getName().equals("WaitingRoom")) {

			System.out.println("Patient komt nu WaitingRoom binnen");
			
			if (((Queue)waitingroom).getNumberInProgress() > waitingroomcapacity) {
				
				System.out.println("Stuur patient weg");
				((Queue)passive).removeEntity(active);
				Entity toGo = active.getJaamSimModel().getNamedEntity("Outside");
				System.out.println(toGo);
				((Linkable)toGo).addEntity(active);

			}	

			else if (((EntityDelay)treatmentroom).getNumberInProgress() == 0  ) { //Send patient to TreatmentRoom

				System.out.println("Stuur patient naar doktor");
				((Queue)passive).removeEntity(active);
				Entity toGo = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
				((Linkable)toGo).addEntity(active);

			}

		}


		else if (active.getName().startsWith("Patient") && passive.getName().equals("Finished")) {

			System.out.println("Patient is nu klaar");

			if (((Queue)waitingroom).getNumberInProgress() > 0  ) {

				System.out.println("Mensen wachten nog");

				DisplayEntity firstinline = ((Queue)waitingroom).getFirst();
				((Queue)waitingroom).removeFirst();
				Entity toGo = active.getJaamSimModel().getNamedEntity("TreatmentRoom");
				((Linkable)toGo).addEntity(firstinline);

			}

		}

	}
	
}
