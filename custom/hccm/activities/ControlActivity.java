package hccm.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.units.DimensionlessUnit;

import hccm.ActivityOrEvent;
import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.events.ActivityEvent;

public class ControlActivity extends EntityDelay implements Activity {
	
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

	class ControlStart extends ActivityEvent {
		
		ControlStart(Activity act) {
			super(act);
		}

		public void happens(List<ActiveEntity> ents) {
			JaamSimModel model = getJaamSimModel();
			EntityDelay ed = (EntityDelay)owner;
			EntityContainer participantEntity = model.createInstance(EntityContainer.class);
			int numCons = 0;
			for (EntityContainer ent : model.getClonesOfIterator(EntityContainer.class))
				numCons++;
			participantEntity.setName(ed.getName() + "_" + numCons);
			participantEntity.setFlag(Entity.FLAG_GENERATED); // Say containers were generated so they get cleared on reset
			participantEntity.setShow(false);
			for (ActiveEntity ent : ents) {
				participantEntity.addEntity(ent);
			}
			ed.addEntity(participantEntity);
		}
		
	}
	
	class ControlFinish extends ActivityEvent {

		ControlFinish(Activity act) {
			super(act);
		}

		@Override
		public void happens(List<ActiveEntity> ents) {
			// Send each entity to its next activity or event
			for (int i=0; i<ents.size(); i++) {
				ActiveEntity ent = ents.get(i);
				ActivityOrEvent actEvt = nextActivityEventList.getValue().get(i);
				ActivityOrEvent.execute(actEvt, ent.asList());
			}
		}
		
	}

	ControlStart startEvent;
	ControlFinish finishEvent;
		
	{
		controlUnitInput = new EntityInput<ControlUnit>(ControlUnit.class, "ControlUnit", Constants.HCCM, null);
		controlUnitInput.setRequired(true);
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

		startEvent = new ControlStart(this);
		finishEvent = new ControlFinish(this);
	}

	@Override
	public ActivityEvent getStartEvent() { return startEvent; }
	@Override
	public ActivityEvent getFinishEvent() { return finishEvent; }
		
	@Override
	public void start(List<ActiveEntity> participants) {
		startEvent.happens(participants);
	}
	
	@Override
	public void sendToNextComponent(DisplayEntity ent) {
		EntityContainer participantEntity = (EntityContainer)ent;
		ArrayList<ActiveEntity> participants = new ArrayList<ActiveEntity>();
		while (!participantEntity.isEmpty(null)) {
			DisplayEntity de = participantEntity.removeEntity(null);
			participants.add((ActiveEntity)de);
		}
		participantEntity.kill();
		
		finish(participants);
	}

	@Override
	public void finish(List<ActiveEntity> participants) {
		finishEvent.happens(participants);
	}
	
	public ControlUnit getControlUnit() { return controlUnitInput.getValue(); }

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
