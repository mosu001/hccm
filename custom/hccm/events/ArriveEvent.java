package hccm.events;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.EntityProviders.EntityProvConstant;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityGenerator;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.InterfaceEntityInput;
import com.jaamsim.input.Keyword;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public class ArriveEvent extends EntityGenerator implements Event {
	
	@Keyword(description = "The activity/event that the arriving entity goes to from this activity.",
	         exampleList = {"Activity1", "Event1"})
	/**
	 * 
	 */
	protected final InterfaceEntityInput<Linkable> nextActivityEvent;

	@Keyword(description = "The triggers that may be executed when this event occurs.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
         + "1 = first trigger, 2 = second trigger, etc.",
         exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;

	{
		nextComponent.setRequired(false);
		nextComponent.setHidden(true);
		
		nextActivityEvent = new InterfaceEntityInput<>(Linkable.class, "NextActivityEvent", Constants.HCCM, null);
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
	 * Overrides parent function, sends entity to next component
	 * @param ent, a DisplayEntity object
	 */
	@Override
	public void sendToNextComponent(DisplayEntity dent) {
		ActiveEntity ent = (ActiveEntity)dent;
		@SuppressWarnings("unchecked")
		EntityProvConstant<DisplayEntity> prov = (EntityProvConstant<DisplayEntity>)getInput("PrototypeEntity").getValue();
		ActiveEntity proto = (ActiveEntity)prov.getEntity();
		ent.setEntityType(proto);
		happens(ent.asList());
	}

	/**
	 * Executes the ArriveEvent assignments
	 */
	public void assigns() { // What changes when this event happens
	}
	
	/**
	 * Executes the ArriveEvent happening
	 * @param ents, a list of ActiveEntity objects
	 */
	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		assigns();
		// Generate a trigger if there is one for this event
		if (triggerList.getValue().size() > 0) {
			double simTime = getSimTime();
  		    Trigger trg = getTrigger(getSimTime());
  		    ControlUnit tcu = trg.getControlUnit();
  		    // Trigger the logic
		    tcu.triggerLogic(trg, ents, simTime);
		}

		// Send this entity to the next activity or event
		Linkable nextCmpt = nextActivityEvent.getValue();
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

}
