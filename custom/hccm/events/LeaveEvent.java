package hccm.events;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntitySink;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.units.DimensionlessUnit;

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
public class LeaveEvent extends EntitySink implements Event {
	
	@Keyword(description = "The (prototype) entities that participate in this activity.",
	         exampleList = {"ProtoEntity1"})
	protected final EntityInput<ActiveEntity> participant;

	@Keyword(description = "The triggers that may be executed when this event occurs.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
        + "1 = first trigger, 2 = second trigger, etc.",
        exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;
	
	@Keyword(description = "Event logger to log start event times when an entity leaves.",
			 exampleList = {"Unit1"})
	private final EntityInput<EventLogger> eventLoggerInput;

	{
		participant = new EntityInput<>(ActiveEntity.class, "Participant", Constants.HCCM, null);
		participant.setRequired(true);
//		participantList.setUnique(false);
		this.addInput(participant);	
		
		triggerList = new EntityListInput<>(Trigger.class, "TriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(triggerList);
		
		triggerChoice = new SampleInput("TriggerChoice", Constants.HCCM, null);
		triggerChoice.setUnitType(DimensionlessUnit.class);
		triggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(triggerChoice);
		
		eventLoggerInput = new EntityInput<EventLogger>(EventLogger.class, "EventLogger", Constants.HCCM, null);
		this.addInput(eventLoggerInput);
	}
	
	/**
	 * Executes the LeaveEvent assignments
	 */
	public void assigns(List<ActiveEntity> ents) { // What changes when this event happens
	}

	/**
	 * Overrides parent EntityDelay method, adds an entity to the process activity.
	 * Note that this assumes only a single entity participates in the process, otherwise
	 * a wait would be needed to join the entities before the process starts
	 * @param ent, a DisplayEntity
	 */
	@Override
	public void addEntity(DisplayEntity ent) {
		ActiveEntity act_ent = (ActiveEntity)ent;
		
		// Ensure that the participant is the correct type of entity.
		ActiveEntity proto = act_ent.getEntityType();
		ActiveEntity proto2 = participant.getValue();
		if (proto != proto2) {
			String msg = "The type of the given entity: '%s', does not match the type required: '%s'\n"
					+ "The error occured in file: '%s', method: '%s', line: '%s'";
			throw new ErrorException(msg, proto.getLocalName(), proto2.getLocalName(),
						Thread.currentThread().getStackTrace()[2].getFileName(),
						Thread.currentThread().getStackTrace()[2].getMethodName(),
						Thread.currentThread().getStackTrace()[2].getLineNumber());
		}
		
		happens(act_ent.asList());
	}
	
	/**
	 * Executes what happens when the LeaveEvent occurs
	 * @param ents, a list of ActiveEntity objects
	 */
	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		
		double simTime = getSimTime();	
		EventLogger eventLogger = eventLoggerInput.getValue();
		
		// All entities involved in this event leave
		for (ActiveEntity ent : ents) {			
			ent.addActivityStart(this.getName());
			ent.addActivityStartTime(simTime);
			
			if (eventLogger != null) {
				eventLogger.recordEntityEvents(ent);
			}
			DisplayEntity de = (DisplayEntity)ent;
			super.addEntity(de);
		}
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//      updateGraphics(getSimTime());
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
