package HCCMLibrary;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.*;

import HCCMLibrary.ControllerHCCM;

/**
 * EntitySink kills the DisplayEntities sent to it.
 */
public class HCCMEntitySink extends LinkedComponent {

	{
		nextComponent.setHidden(true);
		defaultEntity.setHidden(true);
		stateAssignment.setHidden(true);
	}

	@Override
	public void addEntity( DisplayEntity ent ) {
		super.addEntity(ent);

		// Only increments the number process when there is no next entity
		this.sendToNextComponent(ent);
		
		String state = "Event";
		ControllerHCCM.Controller(ent, this, state);

		// Kill the added entity
		if (ent.isGenerated()) {
			ent.kill();
		}
	}

}