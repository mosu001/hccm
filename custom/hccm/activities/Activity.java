package hccm.activities;

import java.util.List;

import hccm.ActivityOrEvent;
import hccm.entities.ActiveEntity;
import hccm.events.ActivityEvent;


public interface Activity extends ActivityOrEvent {

	ActivityEvent getStartEvent();
	ActivityEvent getFinishEvent();
	
	void start(List<ActiveEntity> participants);
	void finish(List<ActiveEntity> participants);
}
