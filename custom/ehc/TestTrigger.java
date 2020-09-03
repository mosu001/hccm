package ehc;

import hccm.activities.WaitActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class TestTrigger extends Trigger {

	/**
	 * Overrides parent class, executes the logic of the triage queue
	 * @param ent, an ActiveEntity object
	 * @param simTime, a double, the sim time
	 */
	@Override
	public void executeLogic(List<ActiveEntity> ents, double simTime) {
            ControlUnit cu = getControlUnit();
        // If there are any requests, then sort them by time
        List<Request> requests = cu.getRequestList();
        if (requests.size() > 1) {
            Collections.sort(requests, RequestUtils::compareWhenRequested);
            // Loop over the requests until a walkup request is found
            Request creq = null;
            for (Request r: requests) {
                if (r.getRequester().getName().startsWith("WalkUpPatient") && r.getRequested().getName().equals("Test")) {
                    creq = r;
                    break;
                }
            }
            // Check if test nurse is free, if so then start triage
            if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTest")).getEntities().isEmpty() ) {
                ActiveEntity cust = creq.getRequester();
                ActiveEntity nurse = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTest")).getEntities().get(0);
                ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, nurse));
                requests.remove(creq);
                creq.getWaiting().finish(cust.asList());
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTest")).finish(nurse.asList());
                creq.getRequested().start(participants);
            }
        }
	}

}
