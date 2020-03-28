package hccm;

import java.util.List;

import com.jaamsim.basicsim.ErrorException;

import hccm.activities.Activity;
import hccm.entities.ActiveEntity;
import hccm.events.Event;

public interface ActivityOrEvent {
	
	public static void execute(ActivityOrEvent actEvt, List<ActiveEntity> ents) {
		if (actEvt instanceof Activity) {
			Activity act = (Activity)actEvt;
			act.start(ents);
		}
		else if (actEvt instanceof Event) {
			Event evt = (Event)actEvt;
			evt.happens(ents);
		}
		else
			throw new ErrorException("Neither Activity nor Event in ActivityOrEvent::execute");
	}

}
