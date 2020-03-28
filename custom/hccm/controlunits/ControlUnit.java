package hccm.controlunits;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;

import hccm.activities.ControlActivity;
import hccm.activities.WaitActivity;
import hccm.entities.ActiveEntity;

public class ControlUnit extends DisplayEntity {
	
	public static class RequestUtils {
	    static public int compareWhenRequested(Request a, Request b) {
	        int x = 0;
	        if (a.whenRequested < b.whenRequested)
	        	x = -1;
	        else
	        	x = 1;

	        return x;
	    }
	}

	public class Request {
		
		ControlActivity whatRequested;
		ActiveEntity whoRequested;
		WaitActivity whereWaiting;
		double whenRequested;
		
		Request(ControlActivity cact, ActiveEntity ent, WaitActivity wact, double simTime) {
			whatRequested = cact;
			whoRequested = ent;
			whereWaiting = wact;
			whenRequested = simTime;
		}
		
		public ControlActivity getRequested()     { return whatRequested; }
		public ActiveEntity    getRequester()     { return whoRequested; }
		public WaitActivity    getWaiting()       { return whereWaiting; }
		double          getTimeRequested() { return whenRequested; }

	}
	
	protected ArrayList<Request> requestList;
	
	{
		requestList = new ArrayList<Request>();
		// TODO: Add events, activities and triggers as inputs to ControlUnits
	}
	
	public List<Request> getRequestList() { return requestList; }
	
	public void requestActivity(ControlActivity cact, ActiveEntity ent, WaitActivity wact, double simTime) {
		requestList.add(new Request(cact, ent, wact, simTime));
	}

	public void triggerLogic(Trigger trg, ActiveEntity ent, double simTime) {
		trg.executeLogic(ent, simTime);
	}
}
