package hccm.events;

import java.util.List;

import hccm.ActivityOrEvent;
import hccm.entities.Entity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public interface Event extends ActivityOrEvent {
	/**
	 * 
	 * @param ents, a list of Entity objects
	 */
	public abstract void happens(List<Entity> ents); // What occurs when this event happens
}
