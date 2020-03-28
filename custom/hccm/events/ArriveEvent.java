package hccm.events;

import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityGenerator;
import com.jaamsim.input.InterfaceEntityInput;
import com.jaamsim.input.Keyword;

import hccm.ActivityOrEvent;
import hccm.Constants;
import hccm.entities.ActiveEntity;

public class ArriveEvent extends EntityGenerator implements Event {
	
	@Keyword(description = "The activity/event that the arriving entity goes to from this activity.",
	         exampleList = {"Activity1", "Event1"})
	protected final InterfaceEntityInput<ActivityOrEvent> nextActivityEvent;

	{
		nextComponent.setRequired(false);
		nextComponent.setHidden(true);
		
		nextActivityEvent = new InterfaceEntityInput<>(ActivityOrEvent.class, "NextActivityEvent", Constants.HCCM, null);
		nextActivityEvent.setRequired(true);
		this.addInput(nextActivityEvent);
	}
	
	@Override
	public void sendToNextComponent(DisplayEntity ent) {
		ActiveEntity actEnt = (ActiveEntity)ent;
		happens(actEnt.asList());
	}

	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens		
		// Send this entity to the next activity or event
		ActivityOrEvent actEvt = nextActivityEvent.getValue();
		ActivityOrEvent.execute(actEvt, ents);
	}
	
}
