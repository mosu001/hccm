package hccm.events;

import java.util.ArrayList;
import java.util.List;

import com.jaamsim.EntityProviders.EntityProvConstant;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProcessFlow.EntityGenerator;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.ErrorException;
import com.jaamsim.input.AssignmentListInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpEvaluator;
import com.jaamsim.input.ExpParser;
import com.jaamsim.input.InterfaceEntityInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
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
public class ArriveEvent extends EntityGenerator implements Event {
	
	@Keyword(description = "A list of attribute assignments that are triggered when an entity arrives.\n\n" +
			"The attributes for various entities can be used in an assignment expression:\n" +
			"- this entity -- this.AttributeName\n" +
			"- entity received -- this.obj.AttributeName\n" +
			"- another entity -- [EntityName].AttributeName",
	         exampleList = {"{ 'this.A = 1' } { 'this.obj.B = 1' } { '[Ent1].C = 1' }",
	                        "{ 'this.D = 1[s] + 0.5*this.SimTime' }"})
	private final AssignmentListInput assignmentList;

	@Keyword(description = "The activity/event/JaamSim object that the arriving entity goes to from this activity.",
	         exampleList = {"Activity1", "Event1"})
	/**
	 * 
	 */
	protected final InterfaceEntityInput<Linkable> nextAEJobject;

	@Keyword(description = "The triggers that may be executed when this event occurs.",
	         exampleList = {"Trigger1"})
	protected final EntityListInput<Trigger> triggerList;

	@Keyword(description = "A number that determines the choice of trigger: "
         + "1 = first trigger, 2 = second trigger, etc.",
         exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleInput triggerChoice;

	private List<ActiveEntity> currentEnts;

	{
		assignmentList = new AssignmentListInput("AssignmentList", Constants.HCCM, new ArrayList<ExpParser.Assignment>());
		this.addInput(assignmentList);

		nextComponent.setRequired(false);
		nextComponent.setHidden(true);
		
		nextAEJobject = new InterfaceEntityInput<>(Linkable.class, "NextAEJObject", Constants.HCCM, null);
		nextAEJobject.setRequired(true);
		this.addInput(nextAEJobject);

		triggerList = new EntityListInput<>(Trigger.class, "TriggerList", Constants.HCCM,
				new ArrayList<Trigger>());
		this.addInput(triggerList);
		
		triggerChoice = new SampleInput("TriggerChoice", Constants.HCCM, null);
		triggerChoice.setUnitType(DimensionlessUnit.class);
		triggerChoice.setValidRange(1, Double.POSITIVE_INFINITY);
		this.addInput(triggerChoice);

		currentEnts = new ArrayList<ActiveEntity>();
	}
	
	/**
	 * Overrides parent function, sends entity to next component
	 * @param ent, a DisplayEntity object
	 */
	@Override
	public void sendToNextComponent(DisplayEntity dent) {
		ActiveEntity ent = (ActiveEntity)dent;
		@SuppressWarnings("unchecked")
		EntityProvConstant<DisplayEntity> prov = (EntityProvConstant<DisplayEntity>)getInput("PrototypeEntity").getValue();
		ActiveEntity proto = (ActiveEntity)prov.getEntity();
		ent.setEntityType(proto);
		happens(ent.asList());
	}

	/**
	 * Executes the ArriveEvent assignments
	 */
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
	
	/**
	 * Executes the ArriveEvent happening
	 * @param ents, a list of ActiveEntity objects
	 */
	public void happens(List<ActiveEntity> ents) { // What occurs when this event happens
		currentEnts = ents;
		assigns(ents);
		// Generate a trigger if there is one for this event
		if (triggerList.getValue().size() > 0) {
			double simTime = getSimTime();
  		    Trigger trg = getTrigger(getSimTime());
  		    ControlUnit tcu = trg.getControlUnit();
  		    // Trigger the logic
		    tcu.triggerLogic(trg, ents, simTime);
		}

		// Send this entity to the next activity or event
		Linkable nextCmpt = nextAEJobject.getValue();
		Constants.nextComponent(this, nextCmpt, ents);
//		System.out.println("Updating graphics for " + getName() + " at " + getSimTime());
//      updateGraphics(getSimTime());
		currentEnts = new ArrayList<ActiveEntity>();
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

	@Output(name = "CurrentParticipants",
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
