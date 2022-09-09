package ehc;

import hccm.activities.ProcessActivity;
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
public class ExtTriageTrigger extends Trigger {

    /**
     * Overrides parent class, executes the logic of the triage queue
     * @param ent, an ActiveEntity object
     * @param simTime, a double, the sim time
     */
    @Override
    public void executeLogic(List<ActiveEntity> ents, double simTime) {
        
        // Check if lunch break (time is between 12:00 and 12:30 and nurse is free
        if ((simTime % 86400) > 43200 && (simTime % 86400) < 45000 && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).getEntities().isEmpty()) {
            ActiveEntity nurse = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).getEntities().get(0);
            ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).finish(nurse.asList());
            ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(nurse));
            ((ProcessActivity)this.getJaamSimModel().getNamedEntity("TriageLunchBreak")).start(participants);
        }
        else {
            ControlUnit cu = getControlUnit();
            // If there are any requests, then sort them by time
            List<Request> requests = cu.getRequestList();
            if (requests.size() > 0) {
                Collections.sort(requests, RequestUtils::compareWhenRequested);
                // Loop over the requests until a walkup request is found
                Request creq = null;
                for (Request r: requests) {
                    if (r.getRequester().getName().startsWith("WalkUpPatient") && r.getRequested().getName().equals("Triage")) {
                        creq = r;
                        break;
                    }
                }
                // Check if triage nurse is free, if so then start triage
                if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).getEntities().isEmpty() ) {
                    ActiveEntity cust = creq.getRequester();
                    ActiveEntity nurse = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).getEntities().get(0);
                    ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, nurse));
                    requests.remove(creq);
                    creq.getWaiting().finish(cust.asList());
                    ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTriage")).finish(nurse.asList());
                    creq.getRequested().start(participants);
                }
            }
        }
    }
}
