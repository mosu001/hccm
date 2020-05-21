package hccm.entities;

import java.util.Arrays;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;

import hccm.activities.Activity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class PassiveEntity extends DisplayEntity implements Entity {

	private PassiveEntity entityType;

	{
		// Passive entities are essentially DisplayEntities with attributes
		// that can be defined and modified
		attributeDefinitionList.setHidden(false);
	}

	/**
	 * Getter function for entityType
	 * @return entityType
	 */
	public Entity getEntityType() {
		return entityType;
	}

	/**
	 * Setter function for entityType
	 * @param entityType
	 */
	public void setEntityType(Entity type) {
		this.entityType = (PassiveEntity)type;
	}
}
