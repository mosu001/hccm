package ssq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.controlunits.ControlUnit.Request;
import hccm.controlunits.ControlUnit.RequestUtils;
import hccm.entities.ActiveEntity;

public class FIFOQTrigger extends Trigger {

	@Override
	public void executeLogic(ActiveEntity ent, double simTime) {
		ControlUnit cu = getControlUnit();
		// If there are any requests, then sort them by time
		List<Request> requests = cu.getRequestList();
		if (requests.size() > 1) {
			Collections.sort(requests, RequestUtils::compareWhenRequested);
			// Loop over the requests until one customer request and one server request are found
			Request creq = null, sreq = null;
	        for (Request r: requests) {
	        	if ( (creq == null) && (r.getRequester().getName().startsWith("Customer")) )
	        		creq = r;
	        	if ( (sreq == null) && (r.getRequester().getName().startsWith("Server")) )
	        		sreq = r;
	        	if ( (creq != null) & (sreq != null) )
	        		break;
	        }
	        // Both a customer and a server have been found waiting
        	if ( (creq != null) & (sreq != null) ) {
		        ActiveEntity cust = creq.getRequester(), serv = sreq.getRequester();
		        ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>(Arrays.asList(cust, serv));
		        creq.getWaiting().finish(cust.asList());
		        requests.remove(creq);
		        sreq.getWaiting().finish(serv.asList());
		        requests.remove(sreq);
		        assert(creq.getRequested().getName().equals(sreq.getRequested().getName()));
		        creq.getRequested().start(participants);
        	}
		}
	}

}
