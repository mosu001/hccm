package hccm.events;

import java.util.List;

import hccm.ActivityOrEvent;
import hccm.entities.ActiveEntity;

public interface Event extends ActivityOrEvent {

	public abstract void happens(List<ActiveEntity> ents); // What occurs when this event happens
}
