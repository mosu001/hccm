package hccm.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jaamsim.ProcessFlow.SimEntity;

import hccm.activities.Activity;

public class ActiveEntity extends SimEntity implements Entity {
	private Activity currentActivity;
	
	{
		currentActivity = null;
	}
	
	// Usual use a list of active entities, this helper
	// function converts a single entity to a list
	public List<ActiveEntity> asList() {
		return Arrays.asList(this);
	}
}
