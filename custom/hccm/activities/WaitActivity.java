package hccm.activities;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.Queue;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.Keyword;
import com.jaamsim.units.DimensionlessUnit;

import hccm.Constants;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.entities.ActiveEntity;
import hccm.events.ActivityEvent;


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
	@Keyword(description = "The activities that may be requested when waiting starts.",
	         exampleList = {"Activity1"})
	protected final EntityListInput<ControlActivity> requestActivityList;

	@Keyword(description = "A number that determines the choice of requested activity: "
            + "1 = first activity, 2 = second activity, etc.",
            exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput requestActivityChoice;

	@Keyword(description = "The triggers that may be executed when waiting starts.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
           + "1 = first trigger, 2 = second trigger, etc.",
           exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;

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
	class WaitStart extends ActivityEvent {
		/**
		 * 
		 * @param act
		 */
		WaitStart(Activity act) {
			super(act);
		}

		/**
		 * Wait activity happens
		 * @param ents, a list of ActiveEntity objects
		 * @exception ErrorException throws errors related to evaluating the assignment expressions
		 */
		public void happens(List<ActiveEntity> ents) {
			WaitActivity act = (WaitActivity)owner;
			double simTime = getSimTime();
			
			// Evaluate the assignment expressions
			for (ExpParser.Assignment ass : startAssignmentList.getValue()) {
				try {
					ExpEvaluator.evaluateExpression(ass, simTime);
				} catch (ExpError err) {
					throw new ErrorException(act, err);
				}
			}

			// Choose the requested activity for this entity
			int i = (int) requestActivityChoice.getValue().getNextSample(simTime);
			if (i<1 || i>requestActivityList.getValue().size())
				error("Chosen index i=%s is out of range for RequestActivityList: %s.",
				      i, requestActivityList.getValue());

			// Get the requested activity
			ControlActivity req = requestActivityList.getValue().get(i-1);
			ControlUnit rcu = req.getControlUnit();
			
			// Choose the trigger for this entity
			i = (int) triggerChoice.getValue().getNextSample(simTime);
			if (i<1 || i>triggerList.getValue().size())
				error("Chosen index i=%s is out of range for TriggerList: %s.",
				      i, triggerList.getValue());

			// Pass the entity to the selected next component
			Trigger trg = triggerList.getValue().get(i-1);
			ControlUnit tcu = trg.getControlUnit();
			
			ActiveEntity ent = ents.get(0);
			
			// Request the activity
			rcu.requestActivity(req, ent, act, simTime);
					
			// Trigger the logic
			tcu.triggerLogic(trg, ent, simTime);
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

		/**
		 * Wait finish happens
		 * @param ents, a list of ActiveEntity objects
		 */
		public void happens(List<ActiveEntity> ents) {
			WaitActivity act = (WaitActivity)owner;
			
			// Evaluate the finish assignment expressions
			for (ExpParser.Assignment ass : finishAssignmentList.getValue()) {
				try {
					ExpEvaluator.evaluateExpression(ass, getSimTime());
				} catch (ExpError err) {
					throw new ErrorException(act, err);
				}
			}
		}
		
	}
	
	WaitStart startEvent;
	WaitFinish finishEvent;
	
	/**
	 * ?
	 */
	{		
		startAssignmentList = new AssignmentListInput("StartAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(startAssignmentList);

		requestActivityList = new EntityListInput<>(ControlActivity.class, "RequestActivityList", Constants.HCCM,
				new ArrayList<ControlActivity>());
		this.addInput(requestActivityList);
		
		requestActivityChoice = new SampleInput("RequestActivityChoice", Constants.HCCM, null);
		requestActivityChoice.setUnitType(DimensionlessUnit.class);
		requestActivityChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(requestActivityChoice);

		triggerList = new EntityListInput<>(Trigger.class, "TriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(triggerList);
		
		triggerChoice = new SampleInput("TriggerChoice", Constants.HCCM, null);
		triggerChoice.setUnitType(DimensionlessUnit.class);
		triggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(triggerChoice);

		finishAssignmentList = new AssignmentListInput("FinishAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(finishAssignmentList);

		startEvent = new WaitStart(this);
		finishEvent = new WaitFinish(this);
	}
	
	/**
	 * Overrides parent function, starts the wait activity
	 * @param ents, a list of ActiveEntity objects
	 */
	@Override
	public void start(List<ActiveEntity> ents) {
		assert(ents.size() == 1);
		addEntity(ents.get(0));
		startEvent.happens(ents);
	}
	
	/**
	 * Overrides parent function, finishes the wait activity
	 * @param ents, a list of ActiveEntity objects
	 */
	@Override
	public void finish(List<ActiveEntity> ents) {
		assert(ents.size() == 1);
		finishEvent.happens(ents);
		removeEntity(ents.get(0));
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
}
