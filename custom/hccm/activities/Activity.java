package hccm.activities;

import java.util.List;

import hccm.ActivityOrEventOrJaamSim;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;
import hccm.events.ActivityEvent;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Activity extends ActivityOrEventOrJaamSim {
	/**
	 * 
	 * @return
	 */
	ActivityEvent getStartEvent();
	ActivityEvent getFinishEvent();
	
	// Only keep track of active entities, passive entities
	// are dealt with by starting and finishing assignments

	void start(List<ActiveEntity> participants);
	void finish(List<ActiveEntity> participants);
	
	void startAssignments(double simTime);
	void finishAssignments(double simTime);

	List<ActiveEntity> getEntities();
	
	String getName();
}
