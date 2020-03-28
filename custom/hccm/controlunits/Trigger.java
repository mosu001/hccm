package hccm.controlunits;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.Keyword;

import hccm.Constants;
import hccm.entities.ActiveEntity;

public abstract class Trigger extends DisplayEntity {
	@Keyword(description = "Control unit this trigger belongs to.",
			 exampleList = {"Unit1"})
	private final EntityInput<ControlUnit> controlUnitInput;
	
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		controlUnitInput.setRequired(true);
		this.addInput(controlUnitInput);
	}

	public ControlUnit getControlUnit() { return controlUnitInput.getValue(); }
	
	public abstract void executeLogic(ActiveEntity ent, double simTime);
}
