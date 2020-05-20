package hccm.events;

import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntitySink;

import hccm.entities.ActiveEntity;
import hccm.entities.Entity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class LeaveEvent extends EntitySink implements Event {

	/**
	 * Executes what happens when the leave event occurs
	 * @param ents, a list of ActiveEntity objects
	 */
	public void happens(List<Entity> ents) { // What occurs when this event happens
		// All entities involved in this event leave
		for (Entity ent : ents) {
			DisplayEntity de = (DisplayEntity)ent;
			addEntity(de);
		}
	}

}
