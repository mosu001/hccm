package hccm.controlunits;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.Keyword;

import hccm.Constants;
import hccm.entities.ActiveEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public abstract class Trigger extends DisplayEntity {
	/**
	 * ?
	 */
	@Keyword(description = "Control unit this trigger belongs to.",
			 exampleList = {"Unit1"})
	
	/**
	 * controlUnitInput constructor?
	 */
	private final EntityInput<ControlUnit> controlUnitInput;
	
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		controlUnitInput.setRequired(true);
		this.addInput(controlUnitInput);
	}

	/**
	 * Getter function for the ControlUnit
	 * @return ControlUnit, the control unit
	 */
	public ControlUnit getControlUnit() { return controlUnitInput.getValue(); }
	
	/**
	 * Abstract function, executes logic?
	 * @param ent, an ActiveEntity
	 * @param simTime, a double
	 */
	public abstract void executeLogic(ActiveEntity ent, double simTime);
}
