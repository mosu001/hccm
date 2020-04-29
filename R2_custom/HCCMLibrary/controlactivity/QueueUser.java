package HCCMLibrary.controlactivity;

import java.util.ArrayList;

//import com.jaamsim.ProcessFlow.Queue;

public interface QueueUser {

	/**
	 * Returns a list of the Queues used by this object.
	 * @return the Queue list.
	 */
	public abstract ArrayList<HCCMControlActivity> getQueues();

	/**
	 * Called whenever an entity is added to one of the Queues used
	 * by this object.
	 */
	public abstract void queueChanged();

}
