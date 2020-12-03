package hccm.activities;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.Queue;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.EntityListListInput;
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
	@Keyword(description = "Lists of activities that may be requested when waiting starts.",
	         exampleList = "{ { Activity1 Activity2 } { Activity3 Activity4 } }")
	protected final EntityListListInput<ProcessActivity> requestActivityList;

	@Keyword(description = "A number that determines the choice of requested activity: "
            + "1 = first activity list, 2 = second activity list, etc.",
            exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput requestActivityChoice;

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
            
			// Choose the requested activity for this entity (if there is one)
			List<ProcessActivity> reqs = null;
			int i;
			if (requestActivityList.getValue().size() >= 1) {
				i = (int) requestActivityChoice.getValue().getNextSample(simTime);
				if (i<1 || i>requestActivityList.getValue().size())
					error("Chosen index i=%s is out of range for RequestActivityList: %s.",
					      i, requestActivityList.getValue());
	
				// Get the requested activity
				reqs = requestActivityList.getValue().get(i-1);
			}
			
	        // Choose the trigger for this entity
			Trigger trg = getTrigger(simTime);
			ControlUnit tcu = null;
			
			ActiveEntity ent = (ActiveEntity)ents.get(0);
			ent.setCurrentActivity(act);
			
			if (reqs != null)
				for (ProcessActivity req : reqs) {
					ControlUnit rcu = req.getControlUnit();
	  			    // Request the activity
                    //System.out.println("Requested activity = " + req.getName());
	  			    rcu.requestActivity(req, ent, act, simTime);
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
	
	WaitStart startEvent;
	WaitFinish finishEvent;
	
	/**
	 * ?
	 */
	{		
		startAssignmentList = new AssignmentListInput("StartAssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(startAssignmentList);

		startTriggerList = new EntityListInput<>(Trigger.class, "StartTriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(startTriggerList);
		
		startTriggerChoice = new SampleInput("StartTriggerChoice", Constants.HCCM, null);
		startTriggerChoice.setUnitType(DimensionlessUnit.class);
		startTriggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(startTriggerChoice);

		requestActivityList = new EntityListListInput<>(ProcessActivity.class, "RequestActivityList", Constants.HCCM,
				new ArrayList<ArrayList<ProcessActivity>>());
		this.addInput(requestActivityList);
		
		requestActivityChoice = new SampleInput("RequestActivityChoice", Constants.HCCM, null);
		requestActivityChoice.setUnitType(DimensionlessUnit.class);
		requestActivityChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(requestActivityChoice);

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
	}
	
	/**
	 * Overrides parent function, starts the wait activity
	 * @param ents, a list of Entity objects
	 */
	@Override
	public void start(List<ActiveEntity> ents) {
		assert(ents.size() == 1);
		System.out.println("In WaitActivity::start " + ents.get(0).getName() + " added to " + getName());
		super.addEntity((DisplayEntity)ents.get(0));
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//      updateGraphics(getSimTime());
		startEvent.happens(ents);
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
		finishEvent.happens(ents);
		removeEntity((DisplayEntity)ents.get(0));
		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
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
	public void finishAssignments(List<ActiveEntity> ents, double simTime) {
		for (ExpParser.Assignment ass : finishAssignmentList.getValue()) {
			try {
				ExpEvaluator.evaluateExpression(ass, simTime);
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
}
