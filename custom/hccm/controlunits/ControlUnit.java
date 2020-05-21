package hccm.controlunits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jaamsim.Graphics.DisplayEntity;

import hccm.activities.ProcessActivity;
import hccm.activities.WaitActivity;
import hccm.entities.ActiveEntity;


/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 *
 */
public class ControlUnit extends DisplayEntity {
	/**
	 * 
	 * @author Michael O'Sullivan
	 * @version 0.0.1
	 * @since 0.0.1
	 *
	 */
	public static class RequestUtils {
		/**
		 * Compares when two requests occurred, and returns an int representing the result 
		 * @param a, a Request
		 * @param b, a Request
		 * @return x, int, 1 if request a is after request b, -1 otherwise
		 */
	    static public int compareWhenRequested(Request a, Request b) {
	        int x = 0;
	        // If a is earlier
	        if (a.whenRequested < b.whenRequested)
	        	x = -1;
	        else
	        	x = 1;

	        return x;
	    }
	}

	/**
	 * 
	 * @author Michael O'Sullivan
	 * @version 0.0.1
	 * @since 0.0.1
	 */
	public class Request {
		/**
		 * variable definitions
		 */
		ProcessActivity whatRequested;
		ActiveEntity whoRequested;
		WaitActivity whereWaiting;
		double whenRequested;
		
		/**
		 * Request constructor
		 * 
		 * @param cact
		 * @param ent
		 * @param wact
		 * @param simTime
		 */
		Request(ProcessActivity cact, ActiveEntity ent, WaitActivity wact, double simTime) {
			whatRequested = cact;
			whoRequested = ent;
			whereWaiting = wact;
			whenRequested = simTime;
		}
		
		/**
		 * Getter method to get ProcessActivity whatRequested
		 * @return ProcessActivity whatRequested
		 */
		public ProcessActivity getRequested()     { return whatRequested; }
		
		/**
		 * Getter method to get ActiveEntity whoRequested
		 * @return ActiveEntity whoRequested
		 */
		public ActiveEntity    getRequester()     { return whoRequested; }
		
		/**
		 * Getter method to get WaitActivity whereWaiting
		 * @return WaitActivity whereWaiting
		 */		
		public WaitActivity    getWaiting()       { return whereWaiting; }
		
		/**
		 * Getter method to get double whenRequested, the simTime
		 * @return double whenRequested
		 */				
		double          getTimeRequested() { return whenRequested; }

	}
	
	/**
	 * requestList, protected, an array of Request objects
	 */
	protected ArrayList<Request> requestList;
	
	/**
	 * Creates requestList
	 */
	{
		requestList = new ArrayList<Request>();
		// TODO: Add events, activities and triggers as inputs to ControlUnits
	}
	
	/**
	 * Getter method to get requestList, the list of requests
	 * @return requestList, the list of requests
	 */
	public List<Request> getRequestList() { return requestList; }
	
	/**
	 * Adds a request to the requestList
	 * @param cact, the control activity
	 * @param ent, the active entity
	 * @param wact, the wait activity
	 * @param simTime, the sim time at the request
	 */
	public void requestActivity(ProcessActivity cact, ActiveEntity ent, WaitActivity wact, double simTime) {
		requestList.add(new Request(cact, ent, wact, simTime));
	}

	/**
	 * Trigger logic
	 * @param trg, the trigger
	 * @param ent, the active entity
	 * @param simTime, the sim time
	 */
	public void triggerLogic(Trigger trg, List<ActiveEntity> ents, double simTime) {
		List<String> entStrs = ents.stream().map(ActiveEntity::getName)
                                            .collect(Collectors.toList());
		String entsAsString = String.join(",", entStrs);
		System.out.println(this.getName() +": Triggered by " + entsAsString);
		trg.executeLogic(ents, simTime);
	}
	
	/**
	 * Overrides the parent earlyInit() function to also clear the requestList
	 */
	@Override
	public void earlyInit() {
		super.earlyInit();
		requestList.clear();
	}
}
