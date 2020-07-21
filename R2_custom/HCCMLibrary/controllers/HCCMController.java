package HCCMLibrary.controllers;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.input.ExpError;

public abstract class HCCMController extends DisplayEntity {

	public abstract void Controller(DisplayEntity active, DisplayEntity passive, String state) throws ExpError;
	
}
