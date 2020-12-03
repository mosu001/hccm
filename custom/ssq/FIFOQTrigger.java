package ssq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jaamsim.basicsim.JaamSimModel;

import hccm.activities.WaitActivity;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.controlunits.ControlUnit.Request;
import hccm.controlunits.ControlUnit.RequestUtils;
import hccm.entities.ActiveEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class FIFOQTrigger extends Trigger {

	/**
	 * Overrides parent class, executes the logic of the FIFO queue
	 * @param ent, an ActiveEntity object
	 * @param simTime, a double, the sim time
	 */
	@Override
	public void executeLogic(List<ActiveEntity> ents, double simTime) {
		ControlUnit cu = getControlUnit();
		// If there are any requests, then sort them by time
		List<Request> requests = cu.getRequestList();
		if (requests.size() > 0) {
			Collections.sort(requests, RequestUtils::compareWhenRequested);
			// Get the first customer request
			Request creq = null;
	        for (Request r: requests) {
	        	System.out.println(r);
	        	if ( (creq == null) && (r.getRequester().getName().startsWith("Customer")) ) {
	        		creq = r;
	        		break;
	        	}
	        }
	        
	        // If a customer request has been found...
        	if (creq != null) {
		        ActiveEntity cust = creq.getRequester(), serv = null;
        		// ... check for a waiting server
        		JaamSimModel model = cu.getJaamSimModel();
        		WaitActivity wait = (WaitActivity)model.getEntity("WaitForCustomer");
        		List<ActiveEntity> waiting = wait.getEntities();
        		if (waiting.size() > 0) {
        			// The server is waiting, pick them
        			// TODO: if more than one server is waiting, then how do you pick, e.g.,
        			// the one waiting longest. Might need to associate a time with
        			// activities an entity is currently participating in (currently can
        			// only be one but this should be extended in the future)
        			serv = waiting.get(0);
        		}
        		if (serv != null) {
			        ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, serv));
			        requests.remove(creq);
			        creq.getWaiting().finish(cust.asList());
			        wait.finish(serv.asList());
			        creq.getRequested().start(participants);
        		}
        	}
		}
	}

}
