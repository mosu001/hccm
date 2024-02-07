package hccm.controlunits;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.StringInput;

import hccm.Constants;
import hccm.entities.ActiveEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class Trigger extends DisplayEntity {

	@Keyword(description = "Control unit this trigger belongs to.",
			 exampleList = {"Unit1"})
	private final EntityInput<ControlUnit> controlUnitInput;
	
	@Keyword(description = "Control policy that trigger executes.",
			 exampleList = {"OnStartWaitToRegsiter"})
	protected final StringInput controlPolicy;
	
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		controlUnitInput.setRequired(true);
		this.addInput(controlUnitInput);
		
		controlPolicy = new StringInput("ControlPolicy", Constants.HCCM, null);
		//controlPolicy.setUnitType(DimensionlessUnit.class);
		//controlPolicy.setRequired(true);
		this.addInput(controlPolicy);
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
	//public abstract void executeLogic(List<ActiveEntity> ents, double simTime);
	
	public void executeLogic(List<ActiveEntity> ents, double simTime) {
		ControlUnit cu = getControlUnit();
		Class<?> c = cu.getClass();
		String methodName = controlPolicy.getValue();
		Class<?>[] paramTypes = {List.class, double.class};
		Method method = null;
		try {
			method = c.getDeclaredMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e1) {
			String msg = "Could not find the method '%s', on control unit '%s'\n";
			msg = String.format(msg, methodName, cu.getLocalName());
			throw new ErrorException(this, msg);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			method.invoke(cu, ents, simTime);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable targetError = e.getTargetException();
			if (targetError instanceof ErrorException) {
				throw (ErrorException) targetError;
			}
			else {
				e.printStackTrace();
			}
		}
	}
	
}
