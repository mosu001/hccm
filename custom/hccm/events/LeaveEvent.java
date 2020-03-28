package hccm.events;

import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntitySink;

import hccm.entities.ActiveEntity;

public class LeaveEvent extends EntitySink implements Event {

	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		// All entities involved in this event leave
		for (ActiveEntity ent : ents) {
			DisplayEntity de = (DisplayEntity)ent;
			addEntity(de);
		}
	}

}
