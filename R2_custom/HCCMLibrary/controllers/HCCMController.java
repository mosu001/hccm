package HCCMLibrary.controllers;

import com.jaamsim.Graphics.DisplayEntity;

public abstract class HCCMController extends DisplayEntity {

	public abstract void Controller(DisplayEntity active, DisplayEntity passive, String state);
	
}
