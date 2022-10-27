package hccm.controlunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.units.DimensionlessUnit;

import hccm.activities.Activity;
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
	
	public class ActivityStartCompare implements Comparator<ActiveEntity> {
				
		public ActivityStartCompare() {
			
		}

		@Override
		public int compare(ActiveEntity ae1, ActiveEntity ae2) {
			double simTime = getSimTime();
			
			double val1 = ae1.getOutputHandle("CurrentActivityStart").getValueAsDouble(simTime, -1);
			double val2 = ae2.getOutputHandle("CurrentActivityStart").getValueAsDouble(simTime, -1);
			
			int ret = Double.compare(val1, val2);
			
			return ret; 
		}		
	}
	
	public class AttributeCompare implements Comparator<ActiveEntity> {
		
		private String attributeName;
		
		public AttributeCompare(String attributeName) {
			this.attributeName = attributeName;
		}

		@Override
		public int compare(ActiveEntity ae1, ActiveEntity ae2) {
			double simTime = getSimTime();
			
			double val1 = ae1.getOutputHandle(attributeName).getValueAsDouble(simTime, -1);
			double val2 = ae2.getOutputHandle(attributeName).getValueAsDouble(simTime, -1);
			
			int ret = Double.compare(val1, val2);
			
			return ret; 
		}		
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
	public Request requestActivity(ProcessActivity cact, ActiveEntity ent, WaitActivity wact, double simTime) {
		Request req = new Request(cact, ent, wact, simTime);
		requestList.add(req);
		return req;
	}

	/**
	 * Trigger logic
	 * @param trg, the trigger
	 * @param ent, the active entity
	 * @param simTime, the sim time
	 */
	public void triggerLogic(Trigger trg, List<ActiveEntity> ents, double simTime) {
//		List<String> entStrs = ents.stream().map(ActiveEntity::getName)
//                                            .collect(Collectors.toList());
//		String entsAsString = String.join(",", entStrs);
//		System.out.println(this.getName() +": Triggered by " + entsAsString);
		trg.executeLogic(ents, simTime);
	}
	
	public ArrayList<ActiveEntity> getEntitiesInState(String entityName, String stateName, double simTime) {
		ArrayList<ActiveEntity> ents = new ArrayList<ActiveEntity>();
		for (ActiveEntity ent : getJaamSimModel().getClonesOfIterator(ActiveEntity.class)) {
			if (ent.getEntityType() != null) {
				String eT = ent.getEntityType().getLocalName();
				String entState = ent.getPresentState(simTime);
				if (entityName.equals(eT) && stateName.equals(entState)) {
					String parName = this.getParent().getLocalName();
					if ("Simulation".equals(parName)) {
						ents.add(ent);
					} else {
						String entSubName = ent.getOutputHandle("Submodel").getValue(simTime, String.class);
						if (entSubName.equals(parName)) {
							ents.add(ent);
						}
					}
					
				}
			}
		}
		return ents;
	}
	
	public ArrayList<ActiveEntity> getEntitiesInActivity(String entityName, String actName, double simTime) {
		Activity act;
		try {
			act = (Activity) getModelEntity(actName);
		} catch (ExpError err) {
			throw new ErrorException(this, err);
		}
		ArrayList<ActiveEntity> ents = (ArrayList<ActiveEntity>) act.getEntities();
		return ents;
	}
	
	public ArrayList<ActiveEntity> getEntitiesInSubmodelActivity(String entityName, String actName, double simTime) {
		String parName = this.getParent().getLocalName();
		if (!"Simulation".equals(parName)) {
			actName = parName + "." + actName;
		}
		ArrayList<ActiveEntity> ents = getEntitiesInActivity(entityName, actName, simTime);
		return ents;
	}
	
	public ArrayList<ActiveEntity> getEntitiesInSubmodelActivities(String entityName, double simTime, String... actNames) {
		ArrayList<ActiveEntity> ents = new ArrayList<ActiveEntity>();
		for (String act: actNames) {
			String parName = this.getParent().getLocalName();
			if (!"Simulation".equals(parName)) {
				act = parName + "." + act;
			}
			ents.addAll(getEntitiesInActivity(entityName, act, simTime));
		}
		return ents;
	}
	
	private Entity getModelEntity(String entityName) throws ExpError {
		Entity ent = this.getJaamSimModel().getNamedEntity(entityName);
		if (ent == null) {	
			String msg = "Could not find model component: '%s'\n"
					+ "The error occured in file: '%s', method: '%s', line: '%s'";
			throw new ExpError(null, 0, msg, entityName,
					Thread.currentThread().getStackTrace()[3].getFileName(),
					Thread.currentThread().getStackTrace()[3].getMethodName(),
					Thread.currentThread().getStackTrace()[3].getLineNumber());
		}
		
		return ent;
	}
	
	public Entity getSubmodelEntity(String entityName) {
		Entity ent;
		String parName = this.getParent().getLocalName();
		try {
			if ("Simulation".equals(parName)) {
				ent = getModelEntity(entityName);
			} else {
				ent = getModelEntity(parName + "." + entityName);
			}
		} catch (ExpError err) {
			throw new ErrorException(this, err);
		}
				
		return ent;
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
