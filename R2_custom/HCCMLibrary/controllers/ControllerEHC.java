package HCCMLibrary.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jaamsim.BasicObjects.InputValue;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.ProbabilityDistributions.Distribution;
import com.jaamsim.ProcessFlow.Linkable;
import com.jaamsim.Samples.TimeSeries;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.input.ExpResult;
import com.jaamsim.input.Output;
import com.jaamsim.units.DimensionlessUnit;

import HCCMLibrary.controlactivity.HCCMControlActivity;
import HCCMLibrary.entities.HCCMActiveEntity;

public class ControllerEHC extends HCCMController {

	// Create needed entities
	DisplayEntity exampleentity =  null;

	// Create needed variables
	Double examplevariable = null;

	// Create needed data Maps (utilisationTimes for this example)
	Map<String, Double> utilisationTimes = new HashMap<String, Double>();

	@Override
	public void earlyInit() {
		super.earlyInit();

		// Get needed entities
		exampleentity = getDisplayEntity("Name of Entity");

		// Get needed variables
		examplevariable = 1.0;
		
		// Clear data Maps (utilisationTimes for this example)
		utilisationTimes.clear();

	}

	@Override
	public void Controller(DisplayEntity activeEntity, DisplayEntity activity, String state){

		// If event happens in system
		if (happens(activeEntity, activity, state, "Name of Active Entity", "Name of Activity", "State of Activity")) {

			System.out.println("This event happens in system");
			
		}
	}
	
		public void startScheduledActvitity (DisplayEntity customer, DisplayEntity server, DisplayEntity activity, double duration) {

			makeServerUnavailable(server);
			((HCCMActiveEntity)server).setPresentState("Working");

			this.scheduleProcess(duration, 5, "sendActivitySignalToList", customer, activity, "EndActivity");
			this.scheduleProcess(duration, 5, "sendActivitySignalToList", server, activity, "EndActivity");
		}

		public void removeEntFrom(DisplayEntity Ent, DisplayEntity From){
			((HCCMControlActivity)From).removeEntity(Ent);
		}

		public void moveEntFromTo(DisplayEntity Ent, DisplayEntity From ,DisplayEntity To) {
			((HCCMControlActivity)From).removeEntity(Ent);
			((Linkable)To).addEntity(Ent);	
		}

		public boolean happens(DisplayEntity active, DisplayEntity passive, String state, String ifactive, String ifpassive, String ifstate) {
			DisplayEntity ifpassiveEnt = getDisplayEntity(ifpassive);
			boolean happen = (active.getName().startsWith(ifactive) && passive.equals(ifpassiveEnt) && state.equals(ifstate));
			return happen;
		}

		public boolean serverAvailable(String server, Entity location) {

			ArrayList <String> matchEntities = ((HCCMControlActivity)location).getMatchValues(getSimTime());
			ArrayList<String> indexes = new ArrayList<String>();

			for(int i=0; i<matchEntities.size(); i++){
				if(matchEntities.get(i).equals(server) && ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i).getOutputHandle("ServerAvailable").getValue(getSimTime(), String.class).equals("1"))
				{               
					String id = Integer.toString(i);
					indexes.add(id);
				}
			}
			int serversAvailable = indexes.size();

			boolean serverAvailable = serversAvailable > 0;
			return serverAvailable;
		}

		public void makeServerAvailable(DisplayEntity server) {

			((HCCMActiveEntity)server).setPresentState("Idle");
			ExpResult eR = ExpResult.makeStringResult("1");
			((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);
		}

		public void makeServerUnavailable(DisplayEntity server) {

			ExpResult eR = ExpResult.makeStringResult("0");
			((DisplayEntity)server).setAttribute("ServerAvailable", null, eR);

		}

		public DisplayEntity getServerAvailable(String server, Entity location) {

			ArrayList <String> matchEntities = ((HCCMControlActivity)location).getMatchValues(getSimTime());
			DisplayEntity serverAvailableEntity = null;

			for(int i=0; i<matchEntities.size(); i++){
				if(matchEntities.get(i).equals(server) && ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i).getOutputHandle("ServerAvailable").getValue(getSimTime(), String.class).equals("1"))
				{               
					serverAvailableEntity = ((HCCMControlActivity)location).getQueueList(getSimTime()).get(i);
					break;
				}
			}
			return serverAvailableEntity;		
		}

		public DisplayEntity getDisplayEntity(String name) {

			Entity ent = this.getJaamSimModel().getNamedEntity(name);
			DisplayEntity dispEnt = ((DisplayEntity)ent);
			return dispEnt;
		}

		public double getInputValue(String name) {

			Entity inputvalueEnt = this.getJaamSimModel().getNamedEntity(name);
			double inputvalue = ((InputValue)inputvalueEnt).getNextSample(getSimTime());
			return inputvalue;
		}

		public double  getDistributionValue(String name) {

			Entity distributionEnt = this.getJaamSimModel().getNamedEntity(name);
			double distributionvalue = ((Distribution)distributionEnt).getNextSample(getSimTime());
			return distributionvalue;
		}

		public double getSimulationRunIndex(int index) {
			double indexvalue = this.getJaamSimModel().getRunIndexList().get(index);
			return indexvalue;
		}

		public double getTimeSeriesValue(String name) {

			Entity timeseriesEnt = getDisplayEntity(name);
			double timeseriesvalue = ((TimeSeries)timeseriesEnt).getPresentValue(getSimTime());
			return timeseriesvalue;
		}

		public double getNextChange(String name) {

			Entity timeseriesEnt = getDisplayEntity(name);
			double nextChange = ((TimeSeries)timeseriesEnt).getNextEventTime(getSimTime());
			return nextChange;
		}

		public void addUtilisation(DisplayEntity server) {

			String serverName = server.getName();

			double initializationDuration = this.getJaamSimModel().getSimulation().getInitializationDuration(getSimTime());

			// Set lastWorkingTime to zero
			if(getSimTime()>=initializationDuration && !utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				utilisationTimes.put("lastWorkingTime"+serverName,0.0); 

			}

			// Set lastWorkinTime to the total time the entity was working before during the initialization period
			else if (getSimTime()<initializationDuration  && !utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				double lastWorkingTime = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working");		
				utilisationTimes.put("lastWorkingTime"+serverName,lastWorkingTime); 

			}

			// Replace lastWorkingTime to the total time the entity was working before during the initialization period
			else if (getSimTime()<initializationDuration && utilisationTimes.containsKey("lastWorkingTime"+serverName)) {

				double lastWorkingTime = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working");		
				utilisationTimes.replace("lastWorkingTime"+serverName,lastWorkingTime); 
			}

			// Calculate the utilisation of the entity after the initialization period and place in utilisationTimes
			if (getSimTime()>=initializationDuration) {

				double runduration = this.getJaamSimModel().getSimulation().getRunDuration();
				double working = ((HCCMActiveEntity)server).getTimeInState(getSimTime(), "Working")-utilisationTimes.get("lastWorkingTime"+serverName);
				double utilisation = working/runduration;

				if (!utilisationTimes.containsKey("Utilisation"+serverName)) {

					utilisationTimes.put("Utilisation"+serverName,utilisation);
				}

				else if (utilisationTimes.containsKey("Utilisation"+serverName)) {

					utilisationTimes.replace("Utilisation"+serverName,utilisation);
				}

			}
		}

		public void sendActivitySignalToList(DisplayEntity active, DisplayEntity passive, String state) {

			if (((HCCMControlActivity)passive).EndActivitySignalList.getValue() != null) {
				for (HCCMController controller : ((HCCMControlActivity)passive).EndActivitySignalList.getValue()) {
					((HCCMController)controller).Controller(active, passive, state);
				}
			}
		}

		public void sendActivitySignal(DisplayEntity controller,DisplayEntity active, DisplayEntity passive, String state) {

			((HCCMController)controller).Controller(active, passive, state);
		}


		@Output(name = "CustomOutput1",
				description = "Custom Output",
				unitType = DimensionlessUnit.class,
				reportable = true,
				sequence = 2)
		public double getCustomOutput1(double simTime) {
			double output = 1;
			return output;
		}
	}
