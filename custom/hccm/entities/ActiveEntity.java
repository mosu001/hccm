package hccm.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jaamsim.ProcessFlow.SimEntity;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

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
	private ActiveEntity entityType;
	private Activity currentActivity;
	private double currentActivityStart;
	
	{
		setEntityType(null);
		setCurrentActivity(null);
		setCurrentActivityStart(0.0);
	}
	
	@Output(name = "EntityType" ,
			unitType = DimensionlessUnit.class ,
			description = "The type of entity that this entity is")
	public String getEntityType(double simTime) {
		return getEntityType().getName();
	}
	
	@Output(name = "CurrentActivity" ,
			unitType = DimensionlessUnit.class ,
			description = "Current activity this entity is participating in")
	public String getCurrentActivity(double simTime) {
		return getCurrentActivity().getName();
	}
	
	@Output(name = "CurrentActivityStart" ,
			unitType = DimensionlessUnit.class ,
			description = "Start time of the current activity this entity is participating in")
	public double getCurrentActivityStart(double simTime) {
		return getCurrentActivityStart();
	}

	
	/**
	 * Helper function that converts a single entity to a list, for use with other functions
	 * @return a list of ActiveEntity objects
	 */
	public List<ActiveEntity> asList() {
		return Arrays.asList(this);
	}

	/**
	 * Getter function for entityType
	 * @return entityType
	 */
	public ActiveEntity getEntityType() {
		return entityType;
	}

	/**
	 * Setter function for entityType
	 * @param entityType
	 */
	public void setEntityType(Entity type) {
		this.entityType = (ActiveEntity)type;
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
	
	/**
	 * Getter function for currentActivityStart
	 * @return currentActivityStart
	 */
	public double getCurrentActivityStart() {
		return currentActivityStart;
	}
	
	/**
	 * Setter function for currentActivityStart
	 * @param simTime
	 */
	public void setCurrentActivityStart(double simTime) {
		this.currentActivityStart = simTime;
	}
	
	
}
