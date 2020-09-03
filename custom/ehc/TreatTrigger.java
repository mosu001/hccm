package ehc;

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

            // Look for a scheduled patient and doctor 2 request
            for (Request r: requests) {
                System.out.println(r);
                if ( (creq == null) && (r.getRequester().getName().startsWith("ScheduledPatient")) )
                        creq = r;
                if ( (sreq == null) && (r.getRequester().getName().startsWith("Doctor2")) && (r.getRequested().getName().equals("TreatScheduled")) )
                        sreq = r;
                if ( (creq != null) & (sreq != null) ) {
                    // Remove doctor 2 walkup request to prevent doubling up
                    for (Request req: requests) {
                        if ((req.getRequester().getName().startsWith("Doctor2")) && req.getRequested().getName().equals("TreatWalkUp2")) {
                            requests.remove(req);
                            break;
                        }
                    }
                    break;
                }
            }

            // If previous not found
            if ( (creq == null) || (sreq == null) ) {
                // Look for a walk up patient and doctor 1 request
                creq = null;
                sreq = null;
                for (Request r: requests) {
                    System.out.println(r);
                    if ( (creq == null) && (r.getRequester().getName().startsWith("WalkUpPatient")) && (r.getRequested().getName().equals("TreatWalkUp")) )
                            creq = r;
                    if ( (sreq == null) && (r.getRequester().getName().startsWith("Doctor1")) )
                            sreq = r;
                    if ( (creq != null) & (sreq != null) ) {
                        // Remove treatwalkup2 walkup request to prevent doubling up
                        for (Request req: requests) {
                            if ((req.getRequester().getName().startsWith("WalkUpPatient")) && req.getRequested().getName().equals("TreatWalkUp2")) {
                                requests.remove(req);
                                break;
                            }
                        }
                        break;
                    }
                }
            }

         // If previous not found
            if ( (creq == null) || (sreq == null) ) {
                // Look for a walkup patient and doctor2 request
                creq = null;
                sreq = null;
                // Loop over the requests until one customer request and one server request are found
                for (Request r: requests) {
                    System.out.println(r);
                    if ( (creq == null) && (r.getRequester().getName().startsWith("WalkUpPatient")) && (r.getRequested().getName().equals("TreatWalkUp2")) )
                            creq = r;
                    if ( (sreq == null) && (r.getRequester().getName().startsWith("Doctor2")) && (r.getRequested().getName().equals("TreatWalkUp2")) )
                            sreq = r;
                    if ( (creq != null) & (sreq != null) ) {
                        // Remove treatwalkup walkup request and treatscheduled doctor2 request to prevent doubling up
                        for (Request req: requests) {
                            if ((req.getRequester().getName().startsWith("WalkUpPatient")) && req.getRequested().getName().equals("TreatWalkUp2")) {
                                requests.remove(req);
                            }
                            else if ((req.getRequester().getName().startsWith("Doctor2")) && req.getRequested().getName().equals("TreatScheduled")) {
                                requests.remove(req);
                            }
                        }
                        break;
                    }
                }
            }

            // Both a customer and a server have been found waiting
            if ( (creq != null) & (sreq != null) ) {
                    ActiveEntity cust = creq.getRequester(), serv = sreq.getRequester();
                    ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, serv));
                    requests.remove(creq);
                    requests.remove(sreq);
                    creq.getWaiting().finish(cust.asList());
                    sreq.getWaiting().finish(serv.asList());
                    assert(creq.getRequested().getName().equals(sreq.getRequested().getName()));
                    creq.getRequested().start(participants);
            }
        }
    }
}
