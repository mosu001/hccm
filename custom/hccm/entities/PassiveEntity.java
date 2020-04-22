package hccm.entities;

import com.jaamsim.Graphics.DisplayEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class PassiveEntity extends DisplayEntity implements Entity {

	{
		// Passive entities are essentially DisplayEntities with attributes
		// that can be defined and modified
		attributeDefinitionList.setHidden(false);
	}
}
