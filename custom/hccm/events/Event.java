package hccm.events;

import java.util.List;

import hccm.ActivityOrEventOrJaamSim;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public interface Event extends ActivityOrEventOrJaamSim {
	/**
	 * 
	 * @param ents, a list of Entity objects
	 */

	// Assignments that occur before an event happens, particularly useful
	// for passive entities' attributes within activities
	public abstract void assigns();
	
	// What occurs when this event happens, manages active entities only,
	// passive entities' attributes dealt with via assignments in activities
	public abstract void happens(List<ActiveEntity> ents);
	
	public abstract Trigger getTrigger(double simTime); // Get the trigger associated with this event
}
