package hccm.events;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityLogger;
import com.jaamsim.ProcessFlow.EntitySink;
import com.jaamsim.ProcessFlow.LinkedComponent;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.FileEntity;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.InterfaceEntityInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class LogEvent extends EntityLogger implements Event {

	protected final InterfaceEntityInput<LinkedComponent> nextActivityEvent;

	@Keyword(description = "The triggers that may be executed when this event occurs.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
         + "1 = first trigger, 2 = second trigger, etc.",
         exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;

	// Unfortunately need to duplicate this from EntityLogger as
	// EntityLogger.addEntity use nextComponent
	private DisplayEntity receivedEntity;

	{
		nextComponent.setRequired(false);
		nextComponent.setHidden(true);
		
		nextActivityEvent = new InterfaceEntityInput<>(LinkedComponent.class, "NextActivityEvent", Constants.HCCM, null);
		nextActivityEvent.setRequired(true);
		this.addInput(nextActivityEvent);

		triggerList = new EntityListInput<>(Trigger.class, "TriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(triggerList);
		
		triggerChoice = new SampleInput("TriggerChoice", Constants.HCCM, null);
		triggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(triggerChoice);
	}
	
	/**
	 * Executes the LeaveEvent assignments
	 */
	public void assigns() { // What changes when this event happens
	}

	/**
	 * Executes what happens when the LeaveEvent occurs
	 * @param ents, a list of ActiveEntity objects
	 */
	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		// All entities involved in this event get logged
		for (ActiveEntity ent : ents) {
			DisplayEntity de = (DisplayEntity)ent;
			addEntity(ent);
		}

		// Generate a trigger if there is one for this event
		if (triggerList.getValue().size() > 0) {
			double simTime = getSimTime();
  		    Trigger trg = getTrigger(getSimTime());
  		    ControlUnit tcu = trg.getControlUnit();
  		    // Trigger the logic
		    tcu.triggerLogic(trg, ents, simTime);
		}

		// Send this entity to the next activity or event
		LinkedComponent nextCmpt = nextActivityEvent.getValue();
		Constants.nextComponent(nextCmpt, ents);
	}
	
	@Override
	public Trigger getTrigger(double simTime) {
		// Choose the trigger for this entity
		int i = (int) triggerChoice.getValue().getNextSample(simTime);
		if (i<1 || i>triggerList.getValue().size())
			error("Chosen index i=%s is out of range for TriggerList: %s.",
			      i, triggerList.getValue());

		// Pass the entity to the selected next component
		Trigger trg = triggerList.getValue().get(i-1);
		
		return trg;
	}

	// Need to override this from EntityLogger as it uses nextComponent
	@Override
	public void addEntity(DisplayEntity ent) {
		receivedEntity = ent;
		
		// Record the entry in the log
		this.recordLogEntry(getSimTime());
	}

	// Need to override this from EntityLogger to use the local copy of
	// receivedEntity
	@Override
	protected void recordEntry(FileEntity file, double simTime) {
		file.format("\t%s", receivedEntity);
	}

	// Need to override this from EntityLogger to use the local copy of
	// receivedEntity
	@Output(name = "obj",
	 description = "The entity that was received most recently.",
	    sequence = 0)
	@Override
	public DisplayEntity getReceivedEntity(double simTime) {
		return receivedEntity;
	}
}
