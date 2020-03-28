package hccm.activities;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityContainer;
import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.InterfaceEntityListInput;
import com.jaamsim.input.Keyword;

import hccm.ActivityOrEvent;
import hccm.Constants;
import hccm.controlunits.ControlUnit;
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

	class ControlStart extends ActivityEvent {
		
		ControlStart(Activity act) {
			super(act);
		}

		public void happens(List<ActiveEntity> ents) {
			JaamSimModel model = getJaamSimModel();
			EntityContainer participantEntity = model.createInstance(EntityContainer.class);
			participantEntity.setShow(false);
			for (ActiveEntity ent : ents) {
				participantEntity.addEntity(ent);
			}
			EntityDelay ed = (EntityDelay)owner;
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

		nextActivityEventList = new InterfaceEntityListInput<>(ActivityOrEvent.class, "NextActivityEventList", Constants.HCCM, null);
		nextActivityEventList.setRequired(true);
		this.addInput(nextActivityEventList);

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
}
