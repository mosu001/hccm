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
public class TreatTrigger extends Trigger {

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
                Request creq = null, sreq = null;

            /*
             * There are three possible actions this trigger can control:
             * 1. Doctor 2 sees scheduled patient (if there are requests from a scheduled patient and doctor2)
             * 2. Doctor 1 sees walkup patient (if there are requests from a walk up patient and doctor1)
             * 3. Doctor 2 sees walkup patient (if there are requests from a walk up patient and doctor2)
             * 
             * We need to check in this order, because we only want doctor 2 to see walkup patients if doctor1
             * is unavailable, otherwise we will waste doctor 1s time. Since doctor 2 is more flexible, it is 
             * better to have him available and assign doctor 1 first.
             */
            
            // Doctor 2 sees scheduled patient
            for (Request r: requests) {
                if (r.getRequester().getName().startsWith("ScheduledPatient") && r.getRequested().getName().equals("TreatScheduled")) {
                    creq = r;
                    break;
                }
            }
            
            if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().isEmpty() ) {
                ActiveEntity cust = creq.getRequester();
                ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().get(0);
                ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, doctor2));
                requests.remove(creq);
                creq.getWaiting().finish(cust.asList());
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                creq.getRequested().start(participants);
            }
            
            // Doctor 1 sees walkup patient
            if (creq == null) {
                for (Request r: requests) {
                    if (r.getRequester().getName().startsWith("WalkUpPatient") && r.getRequested().getName().equals("TreatWalkUp")) {
                        creq = r;
                        break;
                    }
                }

                if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().isEmpty()) {
                    ActiveEntity cust = creq.getRequester();
                    ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().get(0);
                    ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, doctor2));
                    requests.remove(creq);
                    creq.getWaiting().finish(cust.asList());
                    ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).finish(doctor2.asList());
                    creq.getRequested().start(participants);
                }
            }
            
            // Doctor 2 sees walkup patient
            if (creq == null) {
                for (Request r: requests) {
                    if (r.getRequester().getName().startsWith("WalkUpPatient") && r.getRequested().getName().equals("TreatWalkUp2")) {
                        creq = r;
                        break;
                    }
                }

                if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().isEmpty()) {
                    ActiveEntity cust = creq.getRequester();
                    ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().get(0);
                    ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, doctor2));
                    requests.remove(creq);
                    creq.getWaiting().finish(cust.asList());
                    ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                    creq.getRequested().start(participants);
                }
            }
        }    
    }
}
