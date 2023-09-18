package hccm;

import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.Linkable;

import hccm.activities.Activity;
import hccm.entities.ActiveEntity;
import hccm.events.Event;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class Constants {
	/**
	 * Static String HCCM
	 */
	public static final String HCCM = "HCCM";

	public static void nextComponent(Linkable currCmpt, Linkable nextCmpt, List<ActiveEntity> ents) {
		//System.out.println("From " + currCmpt.toString() + " to " + nextCmpt.toString() + " with " + ents.toString());

		if (nextCmpt instanceof Activity) {
			Activity act = (Activity)nextCmpt;
			act.start(ents);
		} else if (nextCmpt instanceof Event) {
			Event evt = (Event)nextCmpt;
			evt.happens(ents);
		} else { // nextCmpt instanceof LinkedComponent
			for (ActiveEntity ent : ents)
				nextCmpt.addEntity((DisplayEntity)ent);
		}
	}

}
