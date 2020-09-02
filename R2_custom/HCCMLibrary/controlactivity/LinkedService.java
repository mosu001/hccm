package HCCMLibrary.controlactivity;

import java.util.ArrayList;

import com.jaamsim.Commands.KeywordCommand;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.StringProviders.StringProvInput;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.EntityInput;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.KeywordIndex;
import com.jaamsim.input.Output;
import com.jaamsim.input.Vec3dInput;
import com.jaamsim.math.Vec3d;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.DistanceUnit;
import com.jaamsim.ProcessFlow.*;

public abstract class LinkedService extends LinkedDevice implements QueueUser {

	@Keyword(description = "The position of the entity being processed relative to the processor.",
	         exampleList = {"1.0 0.0 0.01 m"})
	protected final Vec3dInput processPosition;

	@Keyword(description = "The queue in which the waiting DisplayEntities will be placed.",
	         exampleList = {"Queue1"})
	protected final EntityInput<HCCMControlActivity> waitQueue;

	@Keyword(description = "An expression returning a string value that determines which of the "
	                     + "queued entities are eligible to be selected. "
	                     + "If used, the only entities eligible for selection are the ones whose "
	                     + "inputs for the Queue's Match keyword are equal to value returned by "
	                     + "the expression entered for this Match keyword. "
	                     + "Expressions that return a dimensionless integer or an object are also "
	                     + "valid. The returned number or object is converted to a string "
	                     + "automatically. A floating point number is truncated to an integer."
	                     + "\n\n"
	                     + "Note that a change in the Match value does not trigger the processor "
	                     + "to re-check the Queue.",
	         exampleList = {"this.obj.Attrib1"})
	protected final StringProvInput match;

	private String matchValue;

	{
		stateGraphics.setHidden(false);
		workingStateListInput.setHidden(false);

		processPosition = new Vec3dInput("ProcessPosition", FORMAT, new Vec3d(0.0d, 0.0d, 0.01d));
		processPosition.setUnitType(DistanceUnit.class);
		this.addInput(processPosition);

		waitQueue = new EntityInput<>(HCCMControlActivity.class, "WaitQueue", KEY_INPUTS, null);
		waitQueue.setRequired(true);
		this.addInput(waitQueue);

		match = new StringProvInput("Match", KEY_INPUTS, null);
		match.setUnitType(DimensionlessUnit.class);
		this.addInput(match);
	}

	public LinkedService() {}

	@Override
	public void earlyInit() {
		super.earlyInit();
		matchValue = null;
	}

	@Override
	public void addEntity(DisplayEntity ent) {
		if (isTraceFlag()) trace(0, "addEntity(%s)", ent);

		// If there is no queue, then process the entity immediately
		if (waitQueue.getValue() == null) {
			super.addEntity(ent);
			return;
		}

		// Add the entity to the queue
		waitQueue.getValue().addEntity(ent);
	}

	// ********************************************************************************************
	// SELECTING AN ENTITY FROM THE WAIT QUEUE
	// ********************************************************************************************

	/**
	 * Removes the next entity to be processed from the queue.
	 * If the specified match value is not null, then only the queued entities
	 * with the same match value are eligible to be removed.
	 * @param m - match value.
	 * @return next entity for processing.
	 */
	protected DisplayEntity getNextEntityForMatch(String m) {
		DisplayEntity ent = waitQueue.getValue().removeFirstForMatch(m);
		this.receiveEntity(ent);
		return ent;
	}

	/**
	 * Returns a value which determines which of the entities in the queue are
	 * eligible to be removed. Returns null if the Match keyword has not been set.
	 * @param simTime - present simulation time in seconds.
	 * @return match value.
	 */
	protected String getNextMatchValue(double simTime) {
		if (match.getValue() == null)
			return null;

		return match.getValue().getNextString(simTime, 1.0d, true);
	}

	protected void setMatchValue(String m) {
		matchValue = m;
	}

	protected String getMatchValue() {
		return matchValue;
	}

	// ********************************************************************************************
	// WAIT QUEUE
	// ********************************************************************************************

	public void addQueue(HCCMControlActivity que) {
		if (waitQueue.getHidden()) {
			return;
		}

		ArrayList<String> toks = new ArrayList<>();
		toks.add(que.getName());
		KeywordIndex kw = new KeywordIndex(waitQueue.getKeyword(), toks, null);
		InputAgent.storeAndExecute(new KeywordCommand(this, kw));
	}

	@Override
	public ArrayList<HCCMControlActivity> getQueues() {
		ArrayList<HCCMControlActivity> ret = new ArrayList<>();
		if (waitQueue.getValue() != null)
			ret.add(waitQueue.getValue());
		return ret;
	}

	@Override
	public void queueChanged() {
		if (isTraceFlag()) trace(0, "queueChanged");
		this.restart();
	}

	// ********************************************************************************************
	// DEVICE METHODS
	// ********************************************************************************************

	@Override
	protected void updateProgress(double dt) {}

	@Override
	protected void processChanged() {}

	@Override
	protected boolean isNewStepReqd(boolean completed) {
		return completed;
	}

	@Override
	protected void setProcessStopped() {}

	// ********************************************************************************************
	// GRAPHICS
	// ********************************************************************************************

	protected final void moveToProcessPosition(DisplayEntity ent) {
		ent.setRegion(this.getCurrentRegion());
		Vec3d pos = this.getGlobalPosition();
		pos.add3(processPosition.getValue());
		ent.setGlobalPosition(pos);
		ent.setRelativeOrientation(this.getOrientation());
	}

	@Override
	public ArrayList<DisplayEntity> getSourceEntities() {
		ArrayList<DisplayEntity> ret = super.getSourceEntities();
		ret.addAll(getQueues());
		return ret;
	}

	// ********************************************************************************************
	// OUTPUTS
	// ********************************************************************************************

	@Output(name = "MatchValue",
	 description = "The present value to be matched in the queue.",
	    sequence = 0)
	public String getMatchValue(double simTime) {
		return matchValue;
	}

}
