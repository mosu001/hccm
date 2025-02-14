package hccm.activities;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.Queue;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.BooleanInput;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.EntityListListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.events.ActivityEvent;
import hccm.events.EventLogger;


/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 *
 */
public class WaitActivity extends Queue implements Activity {
	/**
	 * 
	 */
	@Keyword(description = "The (prototype) entities that participate in this activity.",
	         exampleList = {"ProtoEntity1"})
	protected final EntityInput<ActiveEntity> participant;

	@Keyword(description = "A list of attribute assignments that are triggered when an entity starts the activity.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput startAssignmentList;

	@Keyword(description = "The triggers that may be executed when waiting starts.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> startTriggerList;

	@Keyword(description = "A number that determines the choice of starting trigger: "
          + "1 = first trigger, 2 = second trigger, etc.",
          exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput startTriggerChoice;

	@Keyword(description = "A list of attribute assignments that are triggered when an entity finishes the activity.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput finishAssignmentList;

	@Keyword(description = "The triggers that may be executed when waiting finishes.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> finishTriggerList;

	@Keyword(description = "A number that determines the choice of finishing trigger: "
        + "1 = first trigger, 2 = second trigger, etc.",
        exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput finishTriggerChoice;
	
	@Keyword(description = "If TRUE, the start event of the activity will be added to the entity's start times list.",
	         exampleList = {"TRUE"})
	private final BooleanInput logStart;
	
	@Keyword(description = "Event logger to log start event times when an entity leaves.",
			 exampleList = {"Unit1"})
	private final EntityInput<EventLogger> eventLoggerInput;
	
	@Keyword(description = "If TRUE, a trace of the entities and their next activities will be printed to the console.",
	         exampleList = {"TRUE"})
	private final BooleanInput printTrace;

	/**
	 * 
	 * @author Michael O'Sullivan
	 * @version 0.0.1
	 * @since 0.0.1
	 */
	class WaitStart extends ActivityEvent {
		/**
		 * 
		 * @param act
		 */
		WaitStart(Activity act) {
			super(act);
		}

		public void assigns(List<ActiveEntity> ents) {
			double simTime = getSimTime();
			startAssignments(ents, simTime);
		}
		
		/**
		 * Wait activity happens
		 * @param ents, a list of ActiveEntity objects
		 * @exception ErrorException throws errors related to evaluating the assignment expressions
		 */
		public void happens(List<ActiveEntity> ents) {
            assigns(ents);
            
			WaitActivity act = (WaitActivity)owner;
			double simTime = getSimTime();			
            			
	        // Choose the trigger for this entity
			Trigger trg = getTrigger(simTime);
			ControlUnit tcu = null;
			
			ActiveEntity ent = (ActiveEntity)ents.get(0);
			ent.setCurrentActivity(act);
			ent.setCurrentActivityStart(simTime);
			
			if (logStart.getValue() == true) {
				ent.addActivityStart(act.getName());
				ent.addActivityStartTime(simTime);
			}
			
			EventLogger eventLogger = eventLoggerInput.getValue();
			
			if (eventLogger != null) {
				eventLogger.recordEntityEvents(ent);
				ent.initActivityStarts();
				ent.initActivityStartTimes();
			}
								
			if (trg != null) {
				// Trigger the logic
				tcu = trg.getControlUnit();
				tcu.triggerLogic(trg, ents, simTime);
			}
		}
		
		public Trigger getTrigger(double simTime) {
			Trigger trg = null;
			// Choose the trigger for this entity
			boolean trigger = (startTriggerList.getValue().size() > 0);
			if (trigger) {
				if (startTriggerChoice.getValue() == null) {
					String msg = "Trigger list but no trigger choice provided for '%s'";
					throw new ErrorException(msg, owner.getName());
				}
				int i = (int) startTriggerChoice.getNextSample((DisplayEntity)owner, simTime);
				if (i<1 || i>startTriggerList.getValue().size())
					error("Chosen index i=%s is out of range for TriggerList: %s.",
							i, startTriggerList.getValue());

				// Pass the entity to the selected next component
				trg = startTriggerList.getValue().get(i-1);
			}

			return trg;
		}
	}
	
	/**
	 * 
	 * @author Michael O'Sullivan
	 * @version 0.0.1
	 * @since 0.0.1
	 *
	 */
	class WaitFinish extends ActivityEvent {

		WaitFinish(Activity act) {
			super(act);
		}

		public void assigns(List<ActiveEntity> ents) {
			double simTime = getSimTime();
			finishAssignments(ents, simTime);
		}

		/**
		 * Wait finish happens
		 * @param ents, a list of ActiveEntity objects
		 */
		public void happens(List<ActiveEntity> ents) {
			assigns(ents);
			
			double simTime = getSimTime();
			
	        // Choose the trigger for this entity
			Trigger trg = getTrigger(simTime);
			ControlUnit tcu = null;
											
			if (trg != null) {
				// Trigger the logic
				tcu = trg.getControlUnit();
				tcu.triggerLogic(trg, ents, simTime);
			}
		}
		
		public Trigger getTrigger(double simTime) {
			Trigger trg = null;
			// Choose the trigger for this entity
			boolean trigger = (finishTriggerList.getValue().size() > 0);
			if (trigger) {
				int i = (int) finishTriggerChoice.getNextSample((DisplayEntity)owner, simTime);
				if (i<1 || i>finishTriggerList.getValue().size())
					error("Chosen index i=%s is out of range for TriggerList: %s.",
							i, finishTriggerList.getValue());

				// Pass the entity to the selected next component
				trg = finishTriggerList.getValue().get(i-1);
			}

			return trg;
		}
		
		
	}
	
	WaitStart startEvent;
	WaitFinish finishEvent;
	ArrayList<ActiveEntity> finishEnts;
	ArrayList<ActiveEntity> currentParticipant = new ArrayList<ActiveEntity>();;
	/**
	 * ?
	 */
	{	
		participant = new EntityInput<>(ActiveEntity.class, "Participant", Constants.HCCM, null);
		participant.setRequired(true);
//		participantList.setUnique(false);
		this.addInput(participant);		
		
		startAssignmentList = new AssignmentListInput("StartAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(startAssignmentList);

		startTriggerList = new EntityListInput<>(Trigger.class, "StartTriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(startTriggerList);
		
		startTriggerChoice = new SampleInput("StartTriggerChoice", Constants.HCCM, null);
		startTriggerChoice.setUnitType(DimensionlessUnit.class);
		startTriggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(startTriggerChoice);

		finishAssignmentList = new AssignmentListInput("FinishAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(finishAssignmentList);

		finishTriggerList = new EntityListInput<>(Trigger.class, "FinishTriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(finishTriggerList);
		
		finishTriggerChoice = new SampleInput("FinishTriggerChoice", Constants.HCCM, null);
		finishTriggerChoice.setUnitType(DimensionlessUnit.class);
		finishTriggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(finishTriggerChoice);

		startEvent = new WaitStart(this);
		finishEvent = new WaitFinish(this);
		finishEnts = new ArrayList<ActiveEntity>();
		
		logStart = new BooleanInput("LogStart", Constants.HCCM, true);
		this.addInput(logStart);
		
		eventLoggerInput = new EntityInput<EventLogger>(EventLogger.class, "EventLogger", Constants.HCCM, null);
		this.addInput(eventLoggerInput);
		
		printTrace = new BooleanInput("PrintTrace", Constants.HCCM, false);
		this.addInput(printTrace);
	}
	
	/**
	 * Overrides parent function, starts the wait activity
	 * @param ents, a list of Entity objects
	 */
	@Override
	public void start(List<ActiveEntity> ents) {
		assert(ents.size() == 1);
		
		// Ensure that the participant is the correct type of entity.
		ActiveEntity ent = ents.get(0);
		ActiveEntity proto = ent.getEntityType();
		ActiveEntity proto2 = participant.getValue();
		if (proto != proto2) {
			int stackLen = Thread.currentThread().getStackTrace().length;
			int transitionDepth = -1;
			for (int j=0; j<stackLen; j++) {
				System.out.println(Thread.currentThread().getStackTrace()[j].getMethodName());
				if (Thread.currentThread().getStackTrace()[j].getMethodName().equals("transitionTo")) {
					transitionDepth = j+1;
				}
			}
			
			if (transitionDepth == -1) {
				String msg = "For the activity: '%s', the type of the given entity: '%s', does not match the type required: '%s'";
				throw new ErrorException(msg, this.getName(), proto.getLocalName(), proto2.getLocalName());
			} else {
				String msg = "For the activity: '%s', the type of the given entity: '%s', does not match the type required: '%s'\n"
						+ "The error occured in file: '%s', method: '%s', line: '%s'";
				throw new ErrorException(msg, this.getName(), proto.getLocalName(), proto2.getLocalName(),
							Thread.currentThread().getStackTrace()[transitionDepth].getFileName(),
							Thread.currentThread().getStackTrace()[transitionDepth].getMethodName(),
							Thread.currentThread().getStackTrace()[transitionDepth].getLineNumber());
			}
		}

		currentParticipant = ent.asList();
		
		if (printTrace.getValue() == true) {
			System.out.println("In WaitActivity::start " + ents.get(0).getName() + " added to " + getName());
		}
		super.addEntity((DisplayEntity)ents.get(0));
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//      updateGraphics(getSimTime());
		startEvent.happens(ents);
		currentParticipant = null;
	}
	
	/**
	 * Overrides Queue function, starts the wait activity
	 * @param ent, a DisplayEntity
	 */
	@Override
	public void addEntity(DisplayEntity ent) {
		ActiveEntity participant = (ActiveEntity)ent;
		start(participant.asList());
	}

	/**
	 * Overrides parent function, finishes the wait activity
	 * @param ents, a list of ActiveEntity objects
	 */
	@Override
	public void finish(List<ActiveEntity> ents) {
		assert(ents.size() == 1);
		finishEnts = new ArrayList<ActiveEntity>(ents);
		currentParticipant = new ArrayList<ActiveEntity>(ents);
		finishEvent.happens(ents);
		currentParticipant = null;
		removeEntity((DisplayEntity)ents.get(0));
		if (printTrace.getValue() == true) {
			System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
		}
        updateGraphics(getSimTime());
	}
	
	/**
	 * Overrides parent function, getter function for the startEvent object
	 * @return startEvent, the start event object
	 */
	@Override
	public ActivityEvent getStartEvent() {
		return startEvent;
	}	

	/**
	 * Overrides parent function, getter function for the finishEvent object
	 * @return finishEvent, the finish event object
	 */
	@Override
	public ActivityEvent getFinishEvent() {
		return finishEvent;
	}

	/**
	 * Overrides parent function for the startAssignments
	 */
	@Override
	public void startAssignments(List<ActiveEntity> ents, double simTime) {
		for (ExpParser.Assignment ass : startAssignmentList.getValue()) {
			try {
				ExpEvaluator.evaluateExpression(ass, this, simTime);
			} catch (ExpError err) {
				throw new ErrorException(this, err);
			}
		}
	}

	/**
	 * Overrides parent function for the finishAssignments
	 */
	@Override
	public void finishAssignments(List<ActiveEntity> ents, double simTime) {
		for (ExpParser.Assignment ass : finishAssignmentList.getValue()) {
			try {
				ExpEvaluator.evaluateExpression(ass, this, simTime);
			} catch (ExpError err) {
				throw new ErrorException(this, err);
			}
		}
	}

	/**
	 * Overrides parent function, getter function for ents
	 * @return ents, a list of ActiveEntity objects
	 */
	@Override
	public List<ActiveEntity> getEntities() {
		ArrayList<ActiveEntity> ents = new ArrayList<ActiveEntity>();
		for (DisplayEntity de : getQueueList(getSimTime()))
			ents.add((ActiveEntity)de);
		return ents;
	}
	
	@Output(name = "CurrentParticipants",
			 description = "The entity that is currently starting/finishing the activity.",
			    unitType = DimensionlessUnit.class,
			    sequence = 1)
	public ArrayList<ActiveEntity> getCurrentParticipant(double simTime) {
		return currentParticipant;
	}
	
	@Output(name = "FinishingEntities",
	 description = "The entities that are finishing the activity.",
	    unitType = DimensionlessUnit.class,
	    sequence = 2)
	public ArrayList<ActiveEntity> getFinishingEntities(double simTime) {
		return finishEnts;
	}
}
