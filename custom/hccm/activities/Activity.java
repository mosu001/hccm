package hccm.activities;

import java.util.List;

import hccm.ActivityOrEvent;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;
import hccm.events.ActivityEvent;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Activity extends ActivityOrEvent {
	/**
	 * 
	 * @return
	 */
	ActivityEvent getStartEvent();
	ActivityEvent getFinishEvent();
	
	void start(List<Entity> participants);
	void finish(List<Entity> participants);
	
	List<Entity> getEntities();
	
	String getName();
}
