package hccm.entities;

import com.jaamsim.Graphics.DisplayEntity;

public class PassiveEntity extends DisplayEntity implements Entity {

	{
		// Passive entities are essentially DisplayEntities with attributes
		// that can be defined and modified
		attributeDefinitionList.setHidden(false);
	}
}
