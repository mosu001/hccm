package hccm.activities;

import java.util.ArrayList;
import java.util.List;
//import java.util.stream.StreamSupport;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.Assign;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
//import com.jaamsim.Samples.SampleInput;
//import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.BooleanInput;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.ExpParser.Assignment;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
//import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.DimensionlessUnit;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
//import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;
import hccm.events.ActivityEvent;

/**
 * @author Michael O'Sullivan
 * @version 0.1
 * @since 0.1
 */
public class ProcessActivity extends EntityDelay implements Activity {
	
	@Keyword(description = "Control unit this activity belongs to.",
			 exampleList = {"Unit1"})
	private final EntityInput<ControlUnit> controlUnitInput;
	
	@Keyword(description = "The (prototype) entities that participate in this activity.",
	         exampleList = {"ProtoEntity1"})
	protected final EntityListInput<ActiveEntity> participantList;

	@Keyword(description = "The activities/events/JaamSim objects that each of the entities goes to from this activity.",
	         exampleList = {"Activity1"})
	protected final InterfaceEntityListInput<Linkable> nextAEJList;

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
	
	@Keyword(description = "If TRUE, a trace of the entities and their next activities will be printed to the console.",
	         exampleList = {"TRUE"})
	private final BooleanInput printTrace;

	/**
	 * 
	 * @author Michael O'Sullivan
	 * @version 0.0.1
	 * @since 0.0.1
	 */
	class ProcessStart extends ActivityEvent {
		/**
		 * Calls the ActivityEvent DelayStart() method ie. the parent class method
		 * @param act, an activity
		 */
		ProcessStart(Activity act) {
			super(act);
		}

		public void assigns(List<ActiveEntity> ents) {
			double simTime = getSimTime();
			startAssignments(ents, simTime);
		}
		
		/**
		 * ?
		 */
		public void happens(List<ActiveEntity> ents) {			
			JaamSimModel model = getJaamSimModel();
			ProcessActivity act = (ProcessActivity)owner;
			double simTime = getSimTime();
			int numCons = (int) act.getNumberAdded(simTime);
//			for (@SuppressWarnings("unused") EntityContainer ent : model.getClonesOfIterator(EntityContainer.class))
//				numCons++;
			EntityContainer participantEntity = model.createInstance(EntityContainer.class,
					null, act.getName() + "_" + (numCons + 1), null, false, true, false, false);
			participantEntity.setDisplayModelList(null);
			participantEntity.setShow(true);
			for (Entity ent : ents) {
				DisplayEntity de = (DisplayEntity)ent;
				participantEntity.addEntity(de);
			}
						
			currentContainer = participantEntity;
			assigns(ents);
			currentContainer = null;
			
			act.addEntityAsEntityDelay(participantEntity);

			act.setPresentState();
			
			// Choose the trigger for this entity
			Trigger trg = getTrigger(simTime);
			ControlUnit tcu = null;
			
			for (ActiveEntity ent : ents) {
			  ent.setCurrentActivity(act);
			  ent.setCurrentActivityStart(simTime);
			  
			  if (logStart.getValue() == true) {
					ent.addActivityStart(act.getName());
					ent.addActivityStartTime(simTime);
				}
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
	class ProcessFinish extends ActivityEvent {
		/**
		 * Calls the ActivityEvent ControlFinish() method i.e., the parent class method
		 * @param act, an activity
		 */
		ProcessFinish(Activity act) {
			super(act);
		}

		public void assigns(List<ActiveEntity> ents) {
			double simTime = getSimTime();
			finishAssignments(ents, simTime);
		}

		/**
		 * ?
		 * @param ents, a list of active entities
		 */
		@Override
		public void happens(List<ActiveEntity> ents) {
			ProcessActivity act = (ProcessActivity)this.owner;
			double simTime = act.getSimTime();
			assigns(ents);
			
			// Remove entities from the entity container
			while (currentContainer.getCount(simTime) > 0) {
				currentContainer.removeEntity(null);
			}
			// Entities removed, so don't need to keep track of container any longer
			// Kill the container
			if (currentContainer.isGenerated()) {
				currentContainer.kill();
			}
			currentContainer = null;
	
			if (nextAEJList.getValue().size() == 1) {
				// Send all entities to the next activity or event together
			    Linkable nextCmpt = nextAEJList.getValue().get(0);
			    if (printTrace.getValue() == true) {
					for (int i=0; i<ents.size(); i++) {
					  ActiveEntity ent = ents.get(i);
					  ActiveEntity proto = ent.getEntityType();
					  System.out.println("After ProcessActivity " + owner.getName() + ", Entity:" + ent.getName());
					  System.out.println("After ProcessActivity " + owner.getName() + ", proto:" + proto.getName());
	                  if (nextCmpt instanceof Activity)					  
	                	  System.out.println("After ProcessActivity " + owner.getName() + ", Activity:" + ((Activity)nextCmpt).getName());
	                  else
	                	  System.out.println("After ProcessActivity " + owner.getName() + ", Component:" + nextCmpt.toString());
	                }
			    }
				Constants.nextComponent(act, nextCmpt, ents);
			} else {
				// Send each entity to its next activity or event
				for (int i=0; i<ents.size(); i++) {
					ActiveEntity ent = ents.get(i);
					ActiveEntity proto = ent.getEntityType();
					int index = participantList.getValue().indexOf(proto);
					Linkable nextCmpt = nextAEJList.getValue().get(index);
					if (printTrace.getValue() == true) {
						System.out.println("After ProcessActivity " + owner.getName() + ", Entity:" + ent.getName());
						System.out.println("After ProcessActivity " + owner.getName() + ", proto:" + proto.getName());
						System.out.println("After ProcessActivity " + owner.getName() + ", proto index:" + index);
						System.out.println("After ProcessActivity " + owner.getName() + ", participant list:" + participantList.getValue());
						
						if (nextCmpt instanceof Activity)
							System.out.println("After ProcessActivity " + owner.getName() + ", Activity:" + ((Activity)nextCmpt).getName());
						else if (nextCmpt instanceof DisplayEntity)
							System.out.println("After ProcessActivity " + owner.getName() + ", Activity:" + ((DisplayEntity)nextCmpt).getName());
					}
					Constants.nextComponent(act, nextCmpt, ent.asList());
				}
			}

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
	
	ProcessStart startEvent;
	ProcessFinish finishEvent;
	ArrayList<ActiveEntity> currentParticipants = new ArrayList<ActiveEntity>();
	EntityContainer currentContainer;
	/**
	 * 
	 */
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		this.addInput(controlUnitInput);
		
		participantList = new EntityListInput<>(ActiveEntity.class, "ParticipantList", Constants.HCCM, null);
		participantList.setRequired(true);
		participantList.setUnique(false);
		this.addInput(participantList);

		nextComponent.setRequired(false);
		nextComponent.setHidden(true);

		startAssignmentList = new AssignmentListInput("StartAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(startAssignmentList);

		startTriggerList = new EntityListInput<>(Trigger.class, "StartTriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(startTriggerList);
		
		startTriggerChoice = new SampleInput("StartTriggerChoice", Constants.HCCM, null);
		startTriggerChoice.setUnitType(DimensionlessUnit.class);
		startTriggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(startTriggerChoice);

		nextAEJList = new InterfaceEntityListInput<>(Linkable.class, "NextAEJList", Constants.HCCM, null);
		nextAEJList.setRequired(true);
		this.addInput(nextAEJList);

		finishAssignmentList = new AssignmentListInput("FinishAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(finishAssignmentList);

		finishTriggerList = new EntityListInput<>(Trigger.class, "FinishTriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(finishTriggerList);
		
		finishTriggerChoice = new SampleInput("FinishTriggerChoice", Constants.HCCM, null);
		finishTriggerChoice.setUnitType(DimensionlessUnit.class);
		finishTriggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(finishTriggerChoice);

		startEvent = new ProcessStart(this);
		finishEvent = new ProcessFinish(this);
		
		logStart = new BooleanInput("LogStart", Constants.HCCM, true);
		this.addInput(logStart);
		
		printTrace = new BooleanInput("PrintTrace", Constants.HCCM, false);
		this.addInput(printTrace);
	}
	
	/**
	 * Overrides parent ActivityEvent method, getter method for startEvent
	 * @return startEvent
	 */
	@Override
	public ActivityEvent getStartEvent() { return startEvent; }
	
	/**
	 * Overrides parent ActivityEvent method, getter method for finishEvent
	 * @return finishEvent
	 */
	@Override
	public ActivityEvent getFinishEvent() { return finishEvent; }
		
	/**
	 * Overrides parent ActivityEvent method, executes the startEvent
	 * @param participants, a list of ActiveEntity objects that participate in the start event
	 */
	@Override
	public void start(List<ActiveEntity> participants) {
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//		updateGraphics(getSimTime());
		
		// Ensure that the participants are the correct types of entities.
		for (int i=0; i<participants.size(); i++) {
			ActiveEntity ent = participants.get(i);
			ActiveEntity proto = ent.getEntityType();
			ActiveEntity proto2 = participantList.getValue().get(i);
			if (proto != proto2) {
				String msg = "The type of the given entity: '%s', does not match the type required: '%s'\n"
						+ "The error occured in file: '%s', method: '%s', line: '%s'";
				throw new ErrorException(msg, proto.getLocalName(), proto2.getLocalName(),
							Thread.currentThread().getStackTrace()[2].getFileName(),
							Thread.currentThread().getStackTrace()[2].getMethodName(),
							Thread.currentThread().getStackTrace()[2].getLineNumber());
			}
		}
		currentParticipants = (ArrayList<ActiveEntity>) participants;
		startEvent.happens(participants);
	}
	
	/**
	 * Overrides parent EntityDelay method, adds an entity to the process activity.
	 * Note that this assumes only a single entity participates in the process, otherwise
	 * a wait would be needed to join the entities before the process starts
	 * @param ent, a DisplayEntity
	 */
	@Override
	public void addEntity(DisplayEntity ent) {
		ActiveEntity participant = (ActiveEntity)ent;
		start(participant.asList());
	}

	public void addEntityAsEntityDelay(DisplayEntity ent) {
		super.addEntity(ent);
	}

	/**
	 * Overrides parent EntityDelay method, sends an entity to the next component in its process?
	 * @param ent, a DisplayEntity
	 */
	@Override
	public void removeDisplayEntity(DisplayEntity ent) {
		if (printTrace.getValue() == true) {
			System.out.println("In ProcessActivity::removeDisplayEntity..." + this.getEntityList(this.getSimTime()));
		}
		ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>();
	    EntityContainer participantEntity = (EntityContainer)ent;
		double simTime = this.getSimTime();
		for (DisplayEntity de : participantEntity.getEntityList(simTime)) {
			participants.add((ActiveEntity)de);
		}

		// Need to keep track on container to remove entities after assignments
		currentContainer = participantEntity;

		finish(participants);
		super.removeDisplayEntity(ent);
	}	

	/**
	 * Overrides parent ActivityEvent method, executes the finish event by calling .()
	 * @param participants, a list of ActiveEntity objects that participate in the start event
	 */
	@Override
	public void finish(List<ActiveEntity> participants) {
		currentParticipants = (ArrayList<ActiveEntity>) participants;
		finishEvent.happens(participants);
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//		updateGraphics(getSimTime());
	}
	
	/**
	 * Getter method to get the controlUnit of the control activity
	 * @return controlUnitInput.getValue(), the value? 
	 */
	public ControlUnit getControlUnit() { return controlUnitInput.getValue(); }

	/**
	 * Overrides parent function for the startAssignments
	 */
	@Override
	public void startAssignments(List<ActiveEntity> ents, double simTime) {		
		for (ExpParser.Assignment ass : startAssignmentList.getValue()) {
			try {
//				String expString = ass.toString();
//				expString = expString.replace("this.obj", "this");
//				ExpEvaluator.EntityParseContext pc = ExpEvaluator.getParseContext(currentContainer, expString);
//				ExpParser.Assignment mod = ExpParser.parseAssignment(pc, expString);
//				if (printTrace.getValue() == true) {
//					System.out.println("Start assignment is " + expString);
//				}
//				ExpEvaluator.evaluateExpression(mod, this, simTime);
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
//				String expString = ass.toString();
//				int index = getEntityList(simTime).indexOf(currentContainer);
//				expString = expString.replace("this.obj", "this.EntityList(" + (index+1) + ")");
//				// expString = expString.replace("this.obj", "[" + currentContainer.getName() + "]");
//				// for (int i=0; i<ents.size(); i++)
//				//   expString = expString.replace("this.obj.EntityList(" + (i+1) + ")",
//				//                                 "[" + ents.get(i).getName() + "]");
//				// System.out.println(this.getEntityList(simTime));
//				ExpEvaluator.EntityParseContext pc = ExpEvaluator.getParseContext(this, expString);
//				ExpParser.Assignment mod = ExpParser.parseAssignment(pc, expString);
//				if (printTrace.getValue() == true) {
//					System.out.println("Finish assignment is " + mod.toString());
//				}
//				ExpEvaluator.evaluateExpression(mod, this, simTime);
				ExpEvaluator.evaluateExpression(ass, this, simTime);
			} catch (ExpError err) {
				throw new ErrorException(this, err);
			}
		}
	}

	/**
	 * Overrides parent ActivityEvent method, gets the entities of the delay activity
	 * @return ents, a list of ActiveEntity objects
	 */
	@Override
	public List<ActiveEntity> getEntities() {
		double simTime = getSimTime();
		ArrayList<ActiveEntity> ents = new ArrayList<ActiveEntity>();
		for (DisplayEntity de : getEntityList(simTime)) {
			EntityContainer con = (EntityContainer)de;
			for (DisplayEntity cde : con.getEntityList(simTime))
				ents.add((ActiveEntity)cde);
		}
		return ents;
	}

	/**
	 * Gets the participants of the delay activity
	 * @return entArrs, an array of arrays of ActiveEntity objects
	 */
	public ArrayList<ArrayList<ActiveEntity>> getParticipants() {
		double simTime = getSimTime();
		ArrayList<ArrayList<ActiveEntity>> entArrs = new ArrayList<ArrayList<ActiveEntity>>();
		for (DisplayEntity de : getEntityList(simTime)) {
			EntityContainer con = (EntityContainer)de;
			ArrayList<ActiveEntity> ents = new ArrayList<ActiveEntity>();
			for (DisplayEntity cde : con.getEntityList(simTime))
				ents.add((ActiveEntity)cde);
			entArrs.add(ents);
		}
		return entArrs;
	}
	
	@Output(name = "CurrentParticipants",
			 description = "The entities that are currently starting/finishing the activity.",
			    unitType = DimensionlessUnit.class,
			    sequence = 4)
	public ArrayList<ActiveEntity> getCurrentParticipants(double simTime) {
		return currentParticipants;
	}
}