package hccm.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import java.util.stream.StreamSupport;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.ProcessFlow.LinkedComponent;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
//import com.jaamsim.Samples.SampleInput;
//import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;
//import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.DimensionlessUnit;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
//import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.entities.Entity;
import hccm.events.ActivityEvent;
import hccm.events.Event;


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

		public void assigns() {
			double simTime = getSimTime();
			startAssignments(simTime);
		}
		
		/**
		 * ?
		 */
		public void happens(List<ActiveEntity> ents) {			
			JaamSimModel model = getJaamSimModel();
			ProcessActivity act = (ProcessActivity)owner;
			int numCons = 0;
			for (@SuppressWarnings("unused") EntityContainer ent : model.getClonesOfIterator(EntityContainer.class))
				numCons++;
			EntityContainer participantEntity = model.createInstance(EntityContainer.class,
					act.getName() + "_" + (numCons + 1), null, false, true, false, false);
			participantEntity.setDisplayModelList(null);
			participantEntity.setShow(true);
			for (Entity ent : ents) {
				DisplayEntity de = (DisplayEntity)ent;
				participantEntity.addEntity(de);
			}
			act.addEntityAsEntityDelay(participantEntity);
			
			assigns();
			
			double simTime = getSimTime();			

			// Choose the trigger for this entity
			Trigger trg = getTrigger(simTime);
			ControlUnit tcu = null;
			
			for (ActiveEntity ent : ents)
			  ent.setCurrentActivity(act);
								
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
				int i = (int) startTriggerChoice.getValue().getNextSample(simTime);
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

		public void assigns() {
			double simTime = getSimTime();
			finishAssignments(simTime);
		}

		/**
		 * ?
		 * @param ents, a list of active entities
		 */
		@Override
		public void happens(List<ActiveEntity> ents) {
			assigns();
						
			// Now finishing assignments are complete, remove the entity container
			ProcessActivity act = (ProcessActivity)owner;
			EntityContainer ec = act.leavingContainer;
			double simTime = act.getSimTime();
			for (DisplayEntity de : ec.getEntityList(simTime))
				ec.removeEntity(null);
			
			if (nextAEJList.getValue().size() == 1) {
				// Send all entities to the next activity or event together
			    Linkable nextCmpt = nextAEJList.getValue().get(0);				
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
				Constants.nextComponent(nextCmpt, ents);
			} else {
				// Send each entity to its next activity or event
				for (int i=0; i<ents.size(); i++) {
					ActiveEntity ent = ents.get(i);
					ActiveEntity proto = ent.getEntityType();
					int index = participantList.getValue().indexOf(proto);
					System.out.println("After ProcessActivity " + owner.getName() + ", Entity:" + ent.getName());
					System.out.println("After ProcessActivity " + owner.getName() + ", proto:" + proto.getName());
					System.out.println("After ProcessActivity " + owner.getName() + ", proto index:" + index);
					System.out.println("After ProcessActivity " + owner.getName() + ", participant list:" + participantList.getValue());
					Linkable nextCmpt = nextAEJList.getValue().get(index);
					if (nextCmpt instanceof Activity)
						System.out.println("After ProcessActivity " + owner.getName() + ", Activity:" + ((Activity)nextCmpt).getName());
					else if (nextCmpt instanceof DisplayEntity)
						System.out.println("After ProcessActivity " + owner.getName() + ", Activity:" + ((DisplayEntity)nextCmpt).getName());
					Constants.nextComponent(nextCmpt, ent);
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
				int i = (int) finishTriggerChoice.getValue().getNextSample(simTime);
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
	EntityContainer leavingContainer;
	
	/**
	 * 
	 */
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		this.addInput(controlUnitInput);
		
		participantList = new EntityListInput<>(ActiveEntity.class, "ParticipantList", Constants.HCCM, null);
		participantList.setRequired(true);
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
	public void sendToNextComponent(DisplayEntity ent) {
		assert(nextComponent.getValue() == null); // Moving components is achieved using events, so this should be null as it is hidden
		ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>();
	    EntityContainer participantEntity = (EntityContainer)ent;
		for (DisplayEntity de : participantEntity.getEntityList(this.getSimTime())) {
			participants.add((ActiveEntity)de);
		}
		leavingContainer = participantEntity;
		
		for (ActiveEntity pent : participants) {
			System.out.println("Finishing " + this.getName() + " with " + pent.getName());
		}
		finish(participants);
		super.sendToNextComponent(participantEntity);
		participantEntity.kill();
		leavingContainer = null;
	}

	/**
	 * Overrides parent ActivityEvent method, executes the finish event by calling .happens()
	 * @param participants, a list of ActiveEntity objects that participate in the start event
	 */
	@Override
	public void finish(List<ActiveEntity> participants) {
		finishEvent.happens(participants);
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
	public void startAssignments(double simTime) {
		for (ExpParser.Assignment ass : startAssignmentList.getValue()) {
			try {
				ExpEvaluator.evaluateExpression(ass, simTime);
			} catch (ExpError err) {
				throw new ErrorException(this, err);
			}
		}
	}

	/**
	 * Overrides parent function for the finishAssignments
	 */
	@Override
	public void finishAssignments(double simTime) {
		for (ExpParser.Assignment ass : finishAssignmentList.getValue()) {
			try {
				System.out.println("Finish assignment is " + ass.toString());
				ExpEvaluator.evaluateExpression(ass, simTime);
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
}