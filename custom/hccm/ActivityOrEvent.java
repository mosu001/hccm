package hccm;

import java.util.List;

import com.jaamsim.basicsim.ErrorException;

import hccm.activities.Activity;
import hccm.entities.ActiveEntity;
import hccm.events.Event;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ActivityOrEvent {
	
	/**
	 * Executes an activity or event
	 * @param actEvt, the activity or event
	 * @param ents, the list of entities relevant to that activity or event
	 * @exception ErrorException Thrown if the input activity is not an instance of the Activity or Event class
	 */
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
