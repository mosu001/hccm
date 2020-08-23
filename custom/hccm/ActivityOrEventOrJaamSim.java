package hccm;

import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.LinkedComponent;
import com.jaamsim.basicsim.ErrorException;

import hccm.activities.Activity;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;
import hccm.events.Event;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ActivityOrEventOrJaamSim {
	
	/**
	 * Executes an activity or event
	 * @param actEvt, the activity or event
	 * @param ents, the list of entities relevant to that activity or event
	 * @exception ErrorException Thrown if the input activity is not an instance of the Activity or Event class
	 */
	public static void execute(ActivityOrEventOrJaamSim actEvtJS, List<ActiveEntity> ents) {
		if (actEvtJS instanceof Activity) {
			Activity act = (Activity)actEvtJS;
			act.start(ents);
		}
		else if (actEvtJS instanceof Event) {
			Event evt = (Event)actEvtJS;
			evt.happens(ents);
		}
		else { // Assume this is a JaamSim component
			LinkedComponent lc = (LinkedComponent)actEvtJS; // TODO: Check that the component is "allowed"?
			for (Entity ent : ents)
				lc.addEntity((DisplayEntity)ent);
		}
//		throw new ErrorException("Neither Activity nor Event in ActivityOrEvent::execute");
	}
}
