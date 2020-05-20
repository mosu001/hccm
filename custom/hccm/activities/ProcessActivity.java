package hccm.activities;

import java.util.ArrayList;
import java.util.List;
//import java.util.stream.StreamSupport;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityDelay;
//import com.jaamsim.Samples.SampleInput;
//import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;
//import com.jaamsim.units.DimensionlessUnit;

import hccm.ActivityOrEvent;
import hccm.Constants;
import hccm.controlunits.ControlUnit;
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

	@Keyword(description = "The activities/events that each of the entities goes to from this activity.",
	         exampleList = {"Activity1"})
	protected final InterfaceEntityListInput<ActivityOrEvent> nextActivityEventList;

	@Keyword(description = "A list of attribute assignments that are triggered when an entity starts the activity.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput startAssignmentList;

	@Keyword(description = "A list of attribute assignments that are triggered when an entity finishes the activity.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput finishAssignmentList;

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

		/**
		 * ?
		 */
		public void happens(List<Entity> ents) {
			JaamSimModel model = getJaamSimModel();
			EntityDelay ed = (EntityDelay)owner;
			int numCons = 0;
			for (@SuppressWarnings("unused") EntityContainer ent : model.getClonesOfIterator(EntityContainer.class))
				numCons++;
			EntityContainer participantEntity = model.createInstance(EntityContainer.class,
					ed.getName() + "_" + (numCons + 1), null, false, true, false, false);
			participantEntity.setDisplayModelList(null);
			participantEntity.setShow(true);
			for (Entity ent : ents) {
				DisplayEntity de = (DisplayEntity)ent;
				participantEntity.addEntity(de);
				if (ent instanceof ActiveEntity)
					((ActiveEntity)ent).setCurrentActivity(owner);
			}
			ed.addEntity(participantEntity);
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
		 * Calls the ActivityEvent ControlFinish() method ie. the parent class method
		 * @param act, an activity
		 */
		ProcessFinish(Activity act) {
			super(act);
		}

		/**
		 * ?
		 * @param ents, a list of active entities
		 */
		@Override
		public void happens(List<Entity> ents) {
			// Send each entity to its next activity or event
			for (int i=0; i<ents.size(); i++) {
				Entity ent = ents.get(i);
				Entity proto = ent.getEntityType();
				int index = participantList.getValue().indexOf(proto);
				System.out.print("After ProcessActivity, Entity:" + ent.getName());
				System.out.print("After ProcessActivity, proto:" + proto.getName());
				ActivityOrEvent actEvt = nextActivityEventList.getValue().get(index);
				if (actEvt instanceof Activity)
					System.out.print("After ProcessActivity, Activity:" + ((Activity)actEvt).getName());
				ActivityOrEvent.execute(actEvt, ent.asList());
			}
		}
		
	}

	ProcessStart startEvent;
	ProcessFinish finishEvent;
	
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

		nextActivityEventList = new InterfaceEntityListInput<>(ActivityOrEvent.class, "NextActivityEventList", Constants.HCCM, null);
		nextActivityEventList.setRequired(true);
		this.addInput(nextActivityEventList);

		finishAssignmentList = new AssignmentListInput("FinishAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(finishAssignmentList);

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
	public void start(List<Entity> participants) {
		startEvent.happens(participants);
	}
	
	/**
	 * Overrides parent ActivityEvent method, sends an entity to the next component in its process?
	 * @param ent, a DisplayEntity
	 */
	@Override
	public void sendToNextComponent(DisplayEntity ent) {
		EntityContainer participantEntity = (EntityContainer)ent;
		ArrayList<Entity> participants = new ArrayList<Entity>();
		while (!participantEntity.isEmpty(null)) {
			DisplayEntity de = participantEntity.removeEntity(null);
			participants.add((ActiveEntity)de);
		}
		participantEntity.kill();
		
		finish(participants);
	}

	/**
	 * Overrides parent ActivityEvent method, executes the finish event by calling .happens()
	 * @param participants, a list of ActiveEntity objects that participate in the start event
	 */
	@Override
	public void finish(List<Entity> participants) {
		finishEvent.happens(participants);
	}
	
	/**
	 * Getter method to get the controlUnit of the control activity
	 * @return controlUnitInput.getValue(), the value? 
	 */
	public ControlUnit getControlUnit() { return controlUnitInput.getValue(); }

	/**
	 * Overrides parent ActivityEvent method, gets the entities of the delay activity
	 * @return ents, a list of ActiveEntity objects
	 */
	@Override
	public List<Entity> getEntities() {
		double simTime = getSimTime();
		ArrayList<Entity> ents = new ArrayList<Entity>();
		for (DisplayEntity de : getEntityList(simTime)) {
			EntityContainer con = (EntityContainer)de;
			for (DisplayEntity cde : con.getEntityList(simTime))
				ents.add((Entity)cde);
		}
		return ents;
	}

	/**
	 * Gets the participants of the delay activity
	 * @return entArrs, an array of arrays of ActiveEntity objects
	 */
	public ArrayList<ArrayList<Entity>> getParticipants() {
		double simTime = getSimTime();
		ArrayList<ArrayList<Entity>> entArrs = new ArrayList<ArrayList<Entity>>();
		for (DisplayEntity de : getEntityList(simTime)) {
			EntityContainer con = (EntityContainer)de;
			ArrayList<Entity> ents = new ArrayList<Entity>();
			for (DisplayEntity cde : con.getEntityList(simTime))
				ents.add((Entity)cde);
			entArrs.add(ents);
		}
		return entArrs;
	}
}
