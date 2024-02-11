package hccm.events;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.EntityTarget;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.events.EventHandle;
import com.jaamsim.events.EventManager;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

import hccm.Constants;
import hccm.activities.ProcessActivity;
import hccm.activities.WaitActivity;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;

public class LogicEvent extends DisplayEntity implements Event {
	
	@Keyword(description = "The (prototype) entities that participate in this activity.",
	         exampleList = {"ProtoEntity1"})
	protected final EntityListInput<ActiveEntity> participantList;
	
	@Keyword(description = "A list of attribute assignments that are triggered when this event occurs.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput assignmentList;

	@Keyword(description = "The triggers that may be executed when this event occurs.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
	         + "1 = first trigger, 2 = second trigger, etc.",
	         exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;

	private List<ActiveEntity> currentEnts;
	private ArrayList<ScheduledEvent> schedEvents;
	
	{
		participantList = new EntityListInput<>(ActiveEntity.class, "ParticipantList", Constants.HCCM, null);
		participantList.setRequired(true);
		participantList.setUnique(false);
		this.addInput(participantList);	
		
		assignmentList = new AssignmentListInput("AssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(assignmentList);

		triggerList = new EntityListInput<>(Trigger.class, "TriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(triggerList);
		
		triggerChoice = new SampleInput("TriggerChoice", Constants.HCCM, null);
		triggerChoice.setUnitType(DimensionlessUnit.class);
		triggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(triggerChoice);
		
		currentEnts = new ArrayList<ActiveEntity>();
		schedEvents = new ArrayList<ScheduledEvent>();
	}
	
	public class ScheduledEvent {
		/**
		 * variable definitions
		 */
		List<ActiveEntity> whoScheduled;
		double scheduledTime;
		EventHandle eventScheduled;
		
		/**
		 * ScheduleEvent constructor
		 * 
		 * @param ent
		 * @param schedTime
		 * @param evtHand
		 */
		ScheduledEvent(List<ActiveEntity> ents, double schedTime, EventHandle evtHand) {
			whoScheduled = ents;
			scheduledTime = schedTime;
			eventScheduled = evtHand;
		}
		
		public List<ActiveEntity>    	getScheduled()     	{ return whoScheduled; }
		
		public double 			getSchedTime()      { return scheduledTime; }
					
		public EventHandle      getEventHandle()	{ return eventScheduled; }

	}
	
	private static class LogicEventTarget extends EntityTarget<LogicEvent> {
		private final List<ActiveEntity> participatingEnts;
		private final ArrayList<ActiveEntity> le_participantList;

		public LogicEventTarget(LogicEvent le, List<ActiveEntity> ents) {
			super(le, "happens");
			participatingEnts = ents;
			this.le_participantList = le.participantList.getValue();
		}

		@Override
		public void process() {
			// Ensure that the participants are the correct types of entities.
			for (int i=0; i<participatingEnts.size(); i++) {
				ActiveEntity ent = participatingEnts.get(i);
				ActiveEntity proto = ent.getEntityType();
				ActiveEntity proto2 = le_participantList.get(i);
				if (proto != proto2) {
					String msg = "The type of the given entity: '%s', does not match the type required: '%s'\n"
							+ "The error occured in file: '%s', method: '%s', line: '%s'";
					throw new ErrorException(msg, proto.getLocalName(), proto2.getLocalName(),
								Thread.currentThread().getStackTrace()[2].getFileName(),
								Thread.currentThread().getStackTrace()[2].getMethodName(),
								Thread.currentThread().getStackTrace()[2].getLineNumber());
				}
			}
			ent.happens(participatingEnts);
		}
	}

	@Override
	public void assigns(List<ActiveEntity> ents) { // What changes when this event happens
		double simTime = getSimTime();
		for (ExpParser.Assignment ass : assignmentList.getValue()) {
			try {
				ExpEvaluator.evaluateExpression(ass, this, simTime);
			} catch (ExpError err) {
				throw new ErrorException(this, err);
			}
		}
	}

	@Override
	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		currentEnts = ents;
		assigns(ents);
		ScheduledEvent se = getEventFromList(ents);
		schedEvents.remove(se);
		// Generate a trigger if there is one for this event
		if (triggerList.getValue().size() > 0) {
			double simTime = getSimTime();
  		    Trigger trg = getTrigger(getSimTime());
  		    ControlUnit tcu = trg.getControlUnit();
  		    // Trigger the logic
		    tcu.triggerLogic(trg, ents, simTime);
		}
	}

	@Override
	public Trigger getTrigger(double simTime) {
		// Choose the trigger for this entity
		int i = (int) triggerChoice.getNextSample((DisplayEntity)this, simTime);
		if (i<1 || i>triggerList.getValue().size())
			error("Chosen index i=%s is out of range for TriggerList: %s.",
			      i, triggerList.getValue());

		// Pass the entity to the selected next component
		Trigger trg = triggerList.getValue().get(i-1);
		
		return trg;
	}
	
	public void scheduleEvent(List<ActiveEntity> ents, double schedTime) {
		EventHandle hand = new EventHandle();
		ScheduledEvent evt = new ScheduledEvent(ents, schedTime, hand);
		schedEvents.add(evt);
		LogicEventTarget target = new LogicEventTarget(this, ents);
		double simTime = getSimTime();
    	scheduleProcess(schedTime - simTime, 5, true, target, hand); // FIFO
	}
	
	public void rescheduleEvent(List<ActiveEntity> ents, double schedTime) {
		ScheduledEvent se = getEventFromList(ents);
		getJaamSimModel().getEventManager();
		EventManager.killEvent(se.getEventHandle());
		schedEvents.remove(se);
		scheduleEvent(ents, schedTime);
    	//scheduleProcess(schedTime - simTime, 5, true, target, hand); // FIFO
	}
	
	public ScheduledEvent getEventFromList(List<ActiveEntity> ents) {
		ScheduledEvent ret = schedEvents.get(0);
		
		for(ScheduledEvent se : schedEvents)
		{
		    if (se.getScheduled().equals(ents)) {
		    	ret = se;
		    }
		}
		
		return ret;
	}
	
	@Output(name = "EntityList",
	 description = "The entities involved in the event at present.",
	    sequence = 1)
	public ArrayList<DisplayEntity> getEntityList(double simTime) {
		ArrayList<DisplayEntity> ret = new ArrayList<>(currentEnts.size());
		for (ActiveEntity entry : currentEnts) {
			ret.add(entry);
		}
		return ret;
	}

}
