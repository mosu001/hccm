package hccm.activities;

import java.util.List;

import hccm.ActivityOrEvent;
import hccm.entities.ActiveEntity;
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
	
	void start(List<ActiveEntity> participants);
	void finish(List<ActiveEntity> participants);
	
	List<ActiveEntity> getEntities();
}
