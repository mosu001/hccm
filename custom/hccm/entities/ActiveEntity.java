package hccm.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jaamsim.ProcessFlow.SimEntity;

import hccm.activities.Activity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
@SuppressWarnings("unused")
public class ActiveEntity extends SimEntity implements Entity {
	/**
	 * ?
	 */
	private Activity currentActivity;
	
	{
		setCurrentActivity(null);
	}
	
	/**
	 * Helper function that converts a single entity to a list, for use with other functions
	 * @return a list of ActiveEntity objects
	 */
	public List<ActiveEntity> asList() {
		return Arrays.asList(this);
	}

	/**
	 * Getter function for currentActivity
	 * @return currentActivity
	 */
	public Activity getCurrentActivity() {
		return currentActivity;
	}

	/**
	 * Setter function for currentActivity
	 * @param currentActivity
	 */
	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	
	
}
