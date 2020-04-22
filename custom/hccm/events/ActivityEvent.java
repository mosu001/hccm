package hccm.events;

import hccm.activities.Activity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public abstract class ActivityEvent implements Event {
	/**
	 * owner, ?
	 */
	protected Activity owner;
	
	/**
	 * Setter function for owner?
	 * @param act
	 */
	protected ActivityEvent(Activity act) {
		owner = act;
	}

}
