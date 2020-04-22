package abm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jaamsim.ProcessFlow.EntityDelay;
import com.jaamsim.ProcessFlow.Queue;
import com.jaamsim.Samples.SampleConstant;
import com.jaamsim.Samples.SampleInput;
import com.jaamsim.basicsim.JaamSimModel;
import com.jaamsim.input.ExpError;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

import hccm.activities.ControlActivity;
import hccm.activities.WaitActivity;
import hccm.controlunits.ControlUnit;
import hccm.controlunits.Trigger;
import hccm.controlunits.ControlUnit.Request;
import hccm.entities.ActiveEntity;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public class ABMTrigger extends Trigger {
	/**
	 * Variable description
	 */
	@Keyword(description = "The probability distribution for infected individual to uninfected individual transmission.",
	         exampleList = { "0.5", "UniformDistribution1", "'0.5 + 0.5*[UniformDistribution2].Value'" })
	private final SampleInput infectionDistribution;
	/**
	 * Variable description
	 */
	@Keyword(description = "The threshold for testing the infection distribution against.",
	         exampleList = {"0.75", "InputValue1", "[InputValue1].Value"})
	private final SampleInput infectionThreshold;
	
	/**
	 * Number of infections from customer to customer
	 */
	private int numCustToCust;
	/**
	 * Number of infections from customer to server
	 */
	private int numCustToServ;
	/**
	 * Number of infections from server to customer
	 */
	private int numServToCust;
	
	/**
	 * infectionDistribution, the probability distribution for infection?
	 * infectionThreshold, the threshold for infection?
	 */
	{
		infectionDistribution = new SampleInput("TransmissionProbability", KEY_INPUTS, new SampleConstant(DimensionlessUnit.class, 0.0));
		infectionDistribution.setUnitType(DimensionlessUnit.class);
		infectionDistribution.setValidRange(0, 1);
		infectionDistribution.setRequired(true);
		this.addInput(infectionDistribution);
		
		infectionThreshold = new SampleInput("TransmissionThreshold", KEY_INPUTS, null);
		infectionThreshold.setUnitType(DimensionlessUnit.class);
		infectionThreshold.setValidRange(0, 1.0);
		infectionThreshold.setDefaultText("0.5");
		this.addInput(infectionThreshold);
	}

	/**
	 * Overrides Trigger Class earlyInit() function
	 * <p>
	 * Sets numCustToCust, numCustToServ, numServToCust = 0
	 * <p>
	 * @param None
	 * @return None
	 * 
	 */
	@Override
	public void earlyInit( ) {
		super.earlyInit();
		numCustToCust = numCustToServ = numServToCust = 0;
	}
	
	/**
	 * Overrides Trigger Class executeLogic() function
	 * <p>
	 * Executes the logic for a particular entity at a particular time
	 * <p>
	 * @param ent, an ActiveEntity object
	 * @param simTime, the current time in the simulation
	 * @return void
	 */
	@Override
	public void executeLogic(ActiveEntity ent, double simTime) {
		ControlUnit cu = getControlUnit();
		List<Request> requests = cu.getRequestList();
		assert(requests.size() == 1);
		Request req = requests.get(0);
        ActiveEntity timer = req.getRequester();
        req.getWaiting().finish(timer.asList());
	    requests.remove(req);
	    
	    // Implement what happens when the timer "goes off"
	    JaamSimModel model = cu.getJaamSimModel();
	    ArrayList<ControlActivity> controlActs = new ArrayList<ControlActivity>();
	    ArrayList<WaitActivity>    waitActs = new ArrayList<WaitActivity>();
	    for (EntityDelay ed : model.getClonesOfIterator(EntityDelay.class))
	    	if ( (ed instanceof ControlActivity) && (!ed.getName().equals("Ticks")) )
	    		controlActs.add((ControlActivity)ed);
	    for (Queue q : model.getClonesOfIterator(Queue.class))
	    	if ( (q instanceof WaitActivity) && (!q.getName().equals("WaitForTicks")) )
    			waitActs.add((WaitActivity)q);
	    for (ControlActivity cact : controlActs) {
	    	System.out.println("In Timer with " + cact.getName());
	    	int count = 0;
	    	for (List<ActiveEntity> cents : cact.getParticipants()) {
	    		count++;
	    		String entities = cents.stream().map(Object::toString)
                        .collect(Collectors.joining(", "));
				System.out.println("Activity " + count + " = " + entities);
				Interact(cents);
	    	}
	    }
	    // 
	    for (WaitActivity wact : waitActs) {
	    	System.out.println("In Timer with " + wact.getName());
	    	for (ActiveEntity went : wact.getEntities())
	    		System.out.println("Entity waiting = " + went.getName());
	    	Interact(wact.getEntities());
	    }

	    // Start the delay until the next timer trigger
	    req.getRequested().start(timer.asList());
	}

	/**
	 * Does all interactions of entities in a list
	 * 
	 * <p>
	 * For each combination of entities in the given list, calls the Interact()
	 * function {@link #Interact(ActiveEntity, ActiveEntity)}
	 * <p>
	 * 
	 * @param ents, a list of ActiveEntity objects
	 * @return void
	 */
	private void Interact(List<ActiveEntity> ents) {
		if (ents.size() > 1)
			for (int i=0; i<ents.size()-1; i++)
				for (int j=i+1; j<ents.size(); j++) {
					System.out.println("Indices = " + i + ", " + j);
					ActiveEntity ent1 = ents.get(i), ent2 = ents.get(j);
					System.out.println("Entities = " + ent1.getName() + ", " + ent2.getName());
					Interact(ent1, ent2);
				}
		
	}
	
	/**
	 * Executes the interaction between two entities
	 * 
	 * <p>
	 * Models the transmission between two entities. If one is infected and the other is not, there is a chance they will both become infected.
	 * <p>
	 * 
	 * @param ent1, entity 1
	 * @param ent2, entity 2
	 * @exception RuntimeException Thrown if the .setAttribute() method fails on an entity. Prints the variable that was to be set, and the entity it was to be set on.
	 * @return void
	 */
	private void Interact(ActiveEntity ent1, ActiveEntity ent2) {
		double simTime = getSimTime();
		double i1 = ent1.getOutputHandle("Infected").getValueAsDouble(simTime, -1);
		double i2 = ent2.getOutputHandle("Infected").getValueAsDouble(simTime, -1);
		
		if (i1 != i2) {
			// One entity is infected, the other is not, check for transmission
			double probTrans  = infectionDistribution.getValue().getNextSample(simTime);
			double probThresh = infectionThreshold.getValue().getNextSample(simTime);
			if (probTrans > probThresh) {
				// Both entities are now infected
				String infector, infectee;
	            ExpResult r = ExpResult.makeNumResult(1.0, DimensionlessUnit.class);
	            // if i1 is infected
	            if (i1 == 1.0) {
	            	infector = ent1.getName();
	            	infectee = ent2.getName();
	            	// Try to set i2 infected, throws RuntimeException on failure
	            	try {
	            		ent2.setAttribute("Infected", null, r);
	            	} catch (ExpError e) {
	            		throw new RuntimeException("Cannot set Infected attribute to " + r.toString() +
	            				                   "on " + ent2.getName());
	            	}
	            // else i2 should be infected	
	            } else {
	            	assert(i2 == 1.0);
	            	infector = ent2.getName();
	            	infectee = ent1.getName();
	            	// Try to set i1 infected, throws RuntimeException on failure
	            	try {
	            		ent1.setAttribute("Infected", null, r);
	            	} catch (ExpError e) {
	            		throw new RuntimeException("Cannot set Infected attribute to " + r.toString() +
	            				                   "on " + ent1.getName());	            	  	
	            	}
	            }
	            // Updates transmission counts
	            if ( infector.startsWith("Customer") && infectee.startsWith("Customer") )
	            	numCustToCust++;
	            else if ( infector.startsWith("Customer") && infectee.startsWith("Server") )
	            	numCustToServ++;
	            else if ( infector.startsWith("Server") && infectee.startsWith("Customer") )
	            	numServToCust++;
	            		
				System.out.println("Transmission! Entities = " + ent1.getName() + ", " + ent2.getName());
			}			
		} // Otherwise either both are not infected or both are infected so do nothing

	}
	
	/**
	 * A JaamSim Output, the number of cases of transmission
	 * @param simTime
	 * @return 
	 */
	@Output(name = "NumberOfTransmissions",
			 description = "The number of individual to individual transmissions.",
			    unitType = DimensionlessUnit.class,
			    sequence = 1)
			public double getNumTransmissions(double simTime) {
				return (numCustToCust + numCustToServ + numServToCust);
			}

	/**
	 * A JaamSim Output, returns numCustToCust
	 * @param simTime
	 * @return 
	 */
	@Output(name = "NumberOfCustomerToCustomer",
			 description = "The number of customer to customer transmissions.",
			    unitType = DimensionlessUnit.class,
			    sequence = 1)
			public double getNumCustToCust(double simTime) {
				return numCustToCust;
			}

	/**
	 * A JaamSim Output, returns numCustToServ
	 * @param simTime
	 * @return
	 */
	@Output(name = "NumberOfCustomerToServer",
			 description = "The number of customer to server transmissions.",
			    unitType = DimensionlessUnit.class,
			    sequence = 1)
			public double getNumCustToServ(double simTime) {
				return numCustToServ;
			}

	/**
	 * A JaamSim Output, returns numServToCust
	 * @param simTime
	 * @return
	 */
	@Output(name = "NumberOfServerToCustomer",
			 description = "The number of server to customer transmissions.",
			    unitType = DimensionlessUnit.class,
			    sequence = 1)
			public double getNumServToCust(double simTime) {
				return numServToCust;
			}
}
