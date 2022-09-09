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
public class ExtTreatTrigger extends Trigger {

    /**
     * Overrides parent class, executes the logic of the triage queue
     * @param ent, an ActiveEntity object
     * @param simTime, a double, the sim time
     */
    @Override
    public void executeLogic(List<ActiveEntity> ents, double simTime) {
        
        // Check if lunch break (time is between 12:00 and 12:30 and doctors are free
        if ((simTime % 86400) > 43200 && (simTime % 86400) < 45000) {
            if (!((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().isEmpty() && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().isEmpty()) {
                ActiveEntity doctor1 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().get(0);
                ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().get(0);
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).finish(doctor1.asList());
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                ArrayList<ActiveEntity> participants1 = new ArrayList<ActiveEntity>(Arrays.asList(doctor1));
                ((ProcessActivity)this.getJaamSimModel().getNamedEntity("WalkUpLunchBreak")).start(participants1);
                ArrayList<ActiveEntity> participants2 = new ArrayList<ActiveEntity>(Arrays.asList(doctor2));
                ((ProcessActivity)this.getJaamSimModel().getNamedEntity("ScheduledLunchBreak")).start(participants2);                
            }
            else if (!((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().isEmpty()) {
                ActiveEntity doctor1 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().get(0);
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).finish(doctor1.asList());
                ArrayList<ActiveEntity> participants1 = new ArrayList<ActiveEntity>(Arrays.asList(doctor1));
                ((ProcessActivity)this.getJaamSimModel().getNamedEntity("WalkUpLunchBreak")).start(participants1);            
            }
            else if (!((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().isEmpty()) {
                ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().get(0);
                ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                ArrayList<ActiveEntity> participants2 = new ArrayList<ActiveEntity>(Arrays.asList(doctor2));
                ((ProcessActivity)this.getJaamSimModel().getNamedEntity("ScheduledLunchBreak")).start(participants2);                    
            }
        }
        else {
            ControlUnit cu = getControlUnit();
            // If there are any requests, then sort them by time
            List<Request> requests = cu.getRequestList();
            if (requests.size() > 0) {
                Collections.sort(requests, RequestUtils::compareWhenRequested);
                Request creq = null, sreq = null;

                /*
                 * There are four possible actions this trigger can control:
                 * 1. Doctor 1 and Doctor 2 see dual consult walkup patient (if there is a request from a dual consult walkup patient)
                 * 1. Doctor 2 sees scheduled patient (if there are requests from a scheduled patient and doctor2 is free)
                 * 2. Doctor 1 sees walkup patient (if there are requests from a walk up patient and doctor1 is free)
                 * 3. Doctor 2 sees walkup patient (if there are requests from a walk up patient and doctor2 is free)
                 * 
                 * We need to check in this order, because we only want doctor 2 to see walkup patients if doctor1
                 * is unavailable, otherwise we will waste doctor 1s time. Since doctor 2 is more flexible, it is 
                 * better to have him available and assign doctor 1 first.
                 */

                // Doctor 1 and 2 see dual consult walkup patient
                for (Request r: requests) {
                    if (r.getRequester().getName().startsWith("WalkUpPatient") && r.getRequested().getName().equals("DualConsult")) {
                        creq = r;
                        break;
                    }
                }

                if (creq != null && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().isEmpty() && !((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().isEmpty()) {
                    ActiveEntity patient = creq.getRequester();
                    ActiveEntity doctor1 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().get(0);
                    ActiveEntity doctor2 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).getEntities().get(0);
                    ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(patient, doctor1, doctor2));
                    requests.remove(creq);
                    creq.getWaiting().finish(patient.asList());
                    ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).finish(doctor1.asList());
                    ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                    creq.getRequested().start(participants);
                }
                else {
                    creq = null;
                }


                // Doctor 2 sees scheduled patient
                if (creq == null) {
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
                    else {
                        creq = null;
                    }
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
                        ActiveEntity doctor1 = ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).getEntities().get(0);
                        ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, doctor1));
                        requests.remove(creq);
                        Request rreq = null;
                        for (Request r: requests) {
                            if (r.getRequester().getName().equals(creq.getRequester().getName()) && r.getRequested().getName().equals("TreatWalkUp2")) {
                                rreq = r;
                                break;
                            }
                        }
                        if (rreq != null) {
                            requests.remove(rreq);
                        }
                        creq.getWaiting().finish(cust.asList());
                        ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatWalkUp")).finish(doctor1.asList());
                        creq.getRequested().start(participants);
                    }
                    else {
                        creq = null;
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
                        Request rreq = null;
                        for (Request r: requests) {
                            if (r.getRequester().getName().equals(creq.getRequester().getName()) && r.getRequested().getName().equals("TreatWalkUp")) {
                                rreq = r;
                                break;
                            }
                        }
                        if (rreq != null) {
                            requests.remove(rreq);
                        }
                        creq.getWaiting().finish(cust.asList());
                        ((WaitActivity)this.getJaamSimModel().getNamedEntity("WaitToTreatScheduled")).finish(doctor2.asList());
                        creq.getRequested().start(participants);
                    }
                }
            }    
        }
    }
}
