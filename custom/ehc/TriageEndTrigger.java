package ehc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;

import hccm.activities.ProcessActivity;
import hccm.activities.WaitActivity;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.controlunits.ControlUnit.Request;
import hccm.controlunits.ControlUnit.RequestUtils;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;

/**
 * 
 * @author Jack Collinson
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class TriageEndTrigger extends Trigger {

	/**
	 * Overrides parent class, executes the logic of the triage queue
	 * @param ent, an ActiveEntity object
	 * @param simTime, a double, the sim time
	 */

	@Override
	public void executeLogic(List<ActiveEntity> ents, double simTime) {
		// Send the entities to the appropriate process activities
		WaitActivity WaitToTriage = (WaitActivity) this.getJaamSimModel().getNamedEntity("WaitToTriage");
		ProcessActivity MoveToTest = (ProcessActivity) this.getJaamSimModel().getNamedEntity("MoveToTest");
		ProcessActivity MoveToTreat = (ProcessActivity) this.getJaamSimModel().getNamedEntity("MoveToTreat");
		
		for (ActiveEntity e: ents) {
			// Send nurse back to WaitActivity 
			if (e.getName().startsWith("TriageNurse")) {
				WaitToTriage.happens((List<ActiveEntity>) e);
			}
			// If walkup patient needs test, send send them to wait for test 
			else if (Math.abs(e.getOutputHandle("needsTest").getValueAsDouble(getSimTime(), 0.0) - 1.0) < 0.01) {
				MoveToTest.start((List<ActiveEntity>) e);
			}
			// Else, send walk up patient to wait for treatment
			else {
				MoveToTreat.start((List<ActiveEntity>) e);
			}
		}
	}

}
