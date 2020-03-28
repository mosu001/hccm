package hccm.events;

import hccm.activities.Activity;

public abstract class ActivityEvent implements Event {
	protected Activity owner;
		
	protected ActivityEvent(Activity act) {
		owner = act;
	}

}
