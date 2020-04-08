package HCCMLibrary;

import com.jaamsim.Graphics.DisplayEntity;

public class ControllerHCCM extends DisplayEntity {

	public static void Controller(DisplayEntity active, DisplayEntity passive, String state) {

		//ControllerSSQ.Controller(active, passive);
		ControllerTutorialOne.Controller(active, passive, state);
	}

}
