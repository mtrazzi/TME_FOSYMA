package princ;



import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;


import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import env.EntityType;
import env.Environment;
import mas.agents.CollectorAgent;
import mas.agents.DummyExploAgent;
import mas.agents.TankerAgent;
import mas.agents.DummyWumpusShift;
import mas.agents.ExplorerAgent2;

public class Principal {

	private static String hostname = "127.0.0.1"; 
	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Environment env;// static ref of the real environment

	public static void main(String[] args){

		System.out.println("Hello !");
		//0) Create the real environment and the observed one
		//env= new Environment(ENVtype.GRID_T,5,null);
		//env= new Environment(ENVtype.DOROGOVTSEV_T,150,null);
		//env=new Environment("ressources/map2018-2","ressources/map2018-config-2");
		env=new Environment("ressources/map2018-3","ressources/map2018-config-3");
		//env=new Environment("ressources/map2018-Tanker","ressources/map2018-config-Tanker");



		emptyPlatform(containerList);

		//2) create agents and add them to the platform.
		agentList=createAgents(containerList);

		//3) launch agents
		startAgents(agentList);

	}



	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and 3 containers.
	 * 
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(hostname, 8888, null);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}
	/* CreateAgent
	 * 
	 */
	private static List<AgentController> lauchAgent(String agentName,String agentClass, List<AgentController> agentList){
		ContainerController c;
		c = containerList.get("container0");
		try {
			
			switch(agentClass) {
				case "DummyCollectorAgent":
					Object[] objtab=new Object[]{env,EntityType.AGENT_COLLECTOR};//used to give informations to the agent
					AgentController ag=c.createNewAgent(agentName,CollectorAgent.class.getName(),objtab);
					agentList.add(ag);
					break;
				case "DummyExploAgent":
					Object[] objtab1=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
					AgentController ag1=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab1);
					agentList.add(ag1);
					break;
				case "DummyTankerAgent":
					Object[] objtab2=new Object[]{env,EntityType.AGENT_TANKER};//used to give informations to the agent
					AgentController ag2=c.createNewAgent(agentName,TankerAgent.class.getName(),objtab2);
					agentList.add(ag2);
					break;
				case "ExplorerAgent2":
					Object[] objtab4=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
					AgentController ag4=c.createNewAgent(agentName,ExplorerAgent2.class.getName(),objtab4);
					agentList.add(ag4);
					break;
				case "DummyWumpusShift":
					Object[] objtab5=new Object[]{env};//used to give informations to the agent
					AgentController ag5=c.createNewAgent(agentName,DummyWumpusShift.class.getName(),objtab5);
					agentList.add(ag5);
					break;
					
			}
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		return agentList;
	}
	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * 
	 * note: there is a smarter way to find a container with its name, but we go fast to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.


		System.out.println("Launching containers ...");

		//create the container0	
		containerName="container0";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container1	
		containerName="container1";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container2	
		containerName="container2";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		System.out.println("Launching containers done");
		return containerList;
	}


	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 *  - RMA agent's is used to debug and monitor the platform;
	 *  - Sniffer agent is used to monitor communications
	 * @param mc the main-container's reference
	 * @return a ref to the sniffeur agent
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif=null;

		try {
			snif= mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		


	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList.  agents are NOT started.
	 *@param containerList :Name and container's ref
	 *@param sniff : a ref to the sniffeur agent
	 *@return the agentList
	 */
	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		@SuppressWarnings("unused")
		ContainerController c;
		@SuppressWarnings("unused")
		String agentName;
		List<AgentController> agentList=new ArrayList<AgentController>();


		/*
		 * Local and no GateKeeper
		 */

		//	wumpus on container0
//		c = containerList.get("container0");
//		agentName="Golem";
//		try {
//			Object[] objtab=new Object[]{env};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,DummyWumpusShift.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//
//		//	Explorer (no backpack)
//		c = containerList.get("container0");
//		agentName="Agent1";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//
//		//Explorer (no backpack)
//		c = containerList.get("container0");
//		agentName="Agent2";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//		
		//Collector (backPack)
		//agentList = lauchAgent("Agent2","DummyExploAgent", agentList);
		agentList = lauchAgent("Agent3","DummyCollectorAgent", agentList);
		agentList = lauchAgent("Agent4","DummyCollectorAgent",agentList);
		agentList = lauchAgent("Agent5","DummyTankerAgent", agentList);
		//agentList = lauchAgent("Golem","DummyWumpusShift", agentList);
		//c = containerList.get("container0");
		//agentName="Agent3";
		//try {

			//Object[] objtab=new Object[]{env,EntityType.AGENT_COLLECTOR};//used to give informations to the agent
			//AgentController	ag=c.createNewAgent(agentName,DummyCollectorAgent.class.getName(),objtab);
			//agentList.add(ag);
			//System.out.println(agentName+" launched");
	//	} catch (StaleProxyException e) {
		//	e.printStackTrace();
	//	}
//
		//Collector (backPack)
	//	c = containerList.get("container0");
	//	agentName="Agent4";
	//	try {

	//	Object[] objtab=new Object[]{env,EntityType.AGENT_COLLECTOR};//used to give informations to the agent
	//	AgentController	ag=c.createNewAgent(agentName,DummyCollectorAgent.class.getName(),objtab);
	//		agentList.add(ag);
		//	System.out.println(agentName+" launched");
	//} catch (StaleProxyException e) {
		//	e.printStackTrace();
	//	}
		
		//Collector (backPack)
	//	c = containerList.get("container0");
	//	agentName="Agent5";
	//	try {

//		Object[] objtab=new Object[]{env,EntityType.AGENT_COLLECTOR};//used to give informations to the agent
//		AgentController	ag=c.createNewAgent(agentName,DummyCollectorAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//	} catch (StaleProxyException e) {
	//		e.printStackTrace();
//		}

//		//Tanker-Silo (backPack that count for the exam, but not method pick. Can only receive from the collector agents)
//		c = containerList.get("container0");
//		agentName="Agent5";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_TANKER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,DummyTankerAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//
//		//Explorer (no backpack)
//		c = containerList.get("container0");
//		agentName="MyExplorerAgent1";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//		
//		c = containerList.get("container0");
//		agentName="MyExplorerAgent2";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}

//		c = containerList.get("container0");
//		agentName="MyExplorerAgent3";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
//		
//		c = containerList.get("container0");
//		agentName="MyExplorerAgent4";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
		
	/*	c = containerList.get("container0");
		agentName="SmarterExplorerAgent1";
		try {

			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent2.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		c = containerList.get("container0");
		agentName="SmarterExplorerAgent2";
		try {

			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent2.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		c = containerList.get("container0");
		*/
//		agentName="SmarterExplorerAgent3";
//		try {
//
//			Object[] objtab=new Object[]{env,EntityType.AGENT_EXPLORER};//used to give informations to the agent
//			AgentController	ag=c.createNewAgent(agentName,ExplorerAgent2.class.getName(),objtab);
//			agentList.add(ag);
//			System.out.println(agentName+" launched");
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}




		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}

}






