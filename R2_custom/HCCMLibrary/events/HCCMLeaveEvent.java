package HCCMLibrary.events;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.*;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;

import HCCMLibrary.controllers.HCCMController;

/**
 * EntitySink kills the DisplayEntities sent to it.
 */
public class HCCMLeaveEvent extends LinkedComponent {

	// Added
	@Keyword(description = "List of Controllers to which the Event signal is sended.",
			exampleList = {"ExampleController"})
	private final InterfaceEntityListInput<HCCMController> EventSignalList;
	// Added

	{
		nextComponent.setHidden(true);
		defaultEntity.setHidden(true);
		stateAssignment.setHidden(true);

		// Added
		EventSignalList = new InterfaceEntityListInput<>(HCCMController.class, "EventSignalList", "HCCM", null);
		EventSignalList.setRequired(false);
		EventSignalList.setUnique(false);
		this.addInput(EventSignalList);
		// Added

	}

	@Override
	public void addEntity( DisplayEntity ent ) {
		super.addEntity(ent);

		// Only increments the number process when there is no next entity
		this.sendToNextComponent(ent);

		// Added
		if (EventSignalList.getValue() != null) {
			for (HCCMController controller : EventSignalList.getValue()) {
				String state = "Event";
				((HCCMController)controller).Controller(ent, this, state);
			}
		}
		// Added

		// Kill the added entity
		if (ent.isGenerated()) {
			ent.kill();
		}
	}

}