package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Network is a linking of nodes and edges of a powergrid.
 * 
 * @author d3m614
 *
 */
public class Network {
	
	private static Logger log = LoggerFactory.getLogger(Network.class);
	/*
	 * Full network of esca types.
	 */
	private EscaTypes escaTypes;
	private TopologicalNodes topoNodes = new TopologicalNodes();
	
	/**
	 * The set of all connectivity nodes whether processed or unprocessed.
	 */
	private ConnectivityNodes connectivityNodes = new ConnectivityNodes();
	
	/**
	 * The set uf unprocessed connectivity nodes.
	 */
	private ConnectivityNodes unProcessedConnectivityNodes = new ConnectivityNodes();
	
	/**
	 * The set of processed connectivity nodes.
	 */
	private ConnectivityNodes processedConnectivityNodes = new ConnectivityNodes();
	

	public Network(EscaTypes escaTypes){
		log.debug("Creating nework with: " + escaTypes.keySet().size() + " elements.");
		this.escaTypes = escaTypes;
		try {
			
			for(EscaType t: escaTypes.getByResourceType(Esca60Vocab.CONNECTIVITYNODE_OBJECT)){
				connectivityNodes.add((ConnectivityNode)t);
				unProcessedConnectivityNodes.add((ConnectivityNode)t);
			}
			
			this.buildTopology();
		} catch (InvalidArgumentException e) {
			log.error("Error building topology", e);
		}
	}
	
	/**
	 * Notes to myself as building the topology.  
	 * - ConnectivityNodes are directly connected to a VoltageLevel and Substations and 
	 *   referred to by Terminals.  Terminals are the only thing that refers to a 
	 *   ConnectivityNode.
	 *   
	 * - If a ConnectivityNode is directly connected to a Substation then it is not
	 *   directly connected to a VoltageLevel.
	 *   
	 * - If a ConnectivityNode is referred to be a Terminal then it will
	 *   be referred to ba at least two Terminals.
	 *     
	 * - It seems that if a ConnectivityNode is directly connected to a Substation then
	 *   it will not be referred to by any Terminals.
	 *   
	 * - It seems that if a ConnectivityNode is directly connected to a VoltageLevel then
	 *   it will be connected to at least two Terminals.
	 * @throws InvalidArgumentException 
	 */
	private void buildTopology() throws InvalidArgumentException{
		
				
		while (!unProcessedConnectivityNodes.isEmpty()){
			
			// Define a new node/bus
			TopologicalNode topologicalNode = new TopologicalNode();
			topoNodes.add(topologicalNode);
			topologicalNode.setIdentifier("T"+topoNodes.size());
			debugStep("--- Added new topological node");
			
			// Get the first connectivity node that hasn't been processed.
			ConnectivityNode connectivityNode = unProcessedConnectivityNodes.iterator().next();
			debugStep("Processing connectivity Node",  connectivityNode);
								
			// Add the connectivity node to the topological node.
			topologicalNode.addConnectivityNode(connectivityNode);
			processedConnectivityNodes.add(connectivityNode);
			
			// Build list of connected terminals to search over.
			Terminals unprocessedTerminals = connectivityNode.getTerminals(); //new ArrayList<>(connectivityNode.getRefersToMe(Esca60Vocab.TERMINAL_OBJECT));
			debugStep("There are "+unprocessedTerminals.size() + " terminals for connectivity node "+connectivityNode.toString());
			Set<EscaType> processedTerminals = new HashSet<EscaType>();
			
			while(!unprocessedTerminals.isEmpty()){
				// Get the first terminal out of the list.
				Terminal terminal = (Terminal)unprocessedTerminals.iterator().next();
				unprocessedTerminals.remove(0);
				processedTerminals.add(terminal);
				debugStep("Processing terminal",  terminal);
				
				// Equipment associated with the terminal.
				EscaType equipment = terminal.getEquipment(); //.getLink(Esca60Vocab.TERMINAL_CONDUCTINGEQUIPMENT);
				
				if (equipment == null){
					equipment = terminal.getLink(Esca60Vocab.TERMINAL_CONNECTIVITYNODE);
					if (equipment == null){
						System.out.println("No equipment associated with terminal: "+ terminal.getMrid());
						continue;
					}
				}
				
				// Check to see if we have a breaker.
				if (equipment.isResourceType(Esca60Vocab.BREAKER_OBJECT)){
					debugStep("Breaker found", equipment);
					// If the breaker is closed
					if (!equipment.getLiteralValue(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean()){
						debugStep("Breaker was closed", equipment);
						Collection<EscaType> col = equipment.getRefersToMe(Esca60Vocab.TERMINAL_OBJECT);
						for(EscaType e:col){
							if (!processedTerminals.contains(e)){
								debugStep("Adding other side of breaker", e);
								unprocessedTerminals.add((Terminal)e);
							}
						}
					}
					else{
						debugStep("Breaker was open", equipment);
					}
				}
				else if (equipment.isResourceType(Esca60Vocab.CONNECTIVITYNODE_OBJECT)){
					if(processedConnectivityNodes.contains(equipment)){
						debugStep("Something might be wrong here because resource has already been processed.", equipment);
					}
					else{
						debugStep("Adding connectivity node "+ equipment.toString()+ " to toplogical node " + topologicalNode.toString());
						topologicalNode.addConnectivityNode(equipment);
						unProcessedConnectivityNodes.remove(equipment);
						processedConnectivityNodes.add((ConnectivityNode)equipment);
						
						for(EscaType e: equipment.getRefersToMe(Esca60Vocab.TERMINAL_OBJECT)){
							debugStep("Adding terminal to unprocessed", e);
							unprocessedTerminals.add((Terminal)e);
						}
					}
					
				}	
				else{
					debugStep("Unmatched equipment type", equipment);
				}
			}			
			
			processedConnectivityNodes.add(connectivityNode);
		}
		
		
//		int i=1;
//		for(TopologicalNode t: topoNodes){
//			System.out.println("TN "+i);
//			System.out.println("Terminals");
//			for(EscaType terminal: t.getTerminals()){
//				System.out.println("\t"+terminal.getMrid());
//			}
//			System.out.println("CN");
//			for(EscaType cn: t.getConnectivityNodes()){
//				System.out.println("\t"+cn.getMrid());
//			}
//			i++;
//		}
		
//		Collection<EscaType> connectivityNodes = escaTypes.getByResourceType(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
//		TopologicalNode currentTopoNode = null;
//		Set<EscaType> addedConnectivityNodes = new HashSet<>();
//		
//		
//		for(EscaType connNode: connectivityNodes){
//			System.out.println(connNode);
//			for (EscaType t: connNode.getRefersToMe()){
//				System.out.println("REFERS TO ME ("+t.getMrid()+") ***************");
//				System.out.println(t.getRefersToMe());
//				System.out.println("END REFERS TO ME ("+t.getMrid()+") ***************");
//			}
//			//addConnectivityNode(connNode, addedConnectivityNodes, currentTopoNode);
//		}
//		for(EscaType node: connectivityNodes){
//			if ()
//		}
		//debugReferralTree(Esca60Vocab.VOLTAGELEVEL_OBJECT);
		//debugReferralTree(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
//		debugReferralTree(Esca60Vocab.BREAKER_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.BREAKER_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.CONFORMLOAD_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.SYNCHRONOUSMACHINE_OBJECT);
		//debugReferralTree(Esca60Vocab.TERMINAL_OBJECT);
		//debugSetOfDirectConnections(Esca60Vocab.TERMINAL_OBJECT);

		//debugSetOfDirectConnections(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
		//debugSetOfReferralConnections(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
	}
	
	private static void debugStep(String message){
		log.debug(message);
		//System.out.println(message);
	}
	
	private static void debugStep(String message, List<EscaType> typeList){
		log.debug(message);
		debugStep(typeList);
	}
	private static void debugStep(List<EscaType> typeList){
		for(EscaType t:typeList){
			log.debug(t.toString());
		}
	}
	
	private static void debugStep(String message, EscaType escaType){
		if (escaType.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME) != null){
			log.debug(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ") ["+escaType.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME)+ "]");
			//System.out.println(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ") ["+escaType.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME)+ "]");
		}
		else{
			log.debug(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ")");
			//System.out.println(message+" "+escaType.getDataType()+ " ("+escaType.getMrid()+ ")");
		}
	}
	
	private void debugSetOfLiterals(Resource resourceType){
		Set<String> properties = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(String v: t.getLiterals().keySet()){
				properties.add(v);
			}
		}
		log.debug("The set of literals for "+resourceType.getLocalName());
		for(String s: properties){
			log.debug(s);
		}
	}
	
	private void debugSetOfReferralConnections(Resource resourceType){
		Set<String> dataTypeSet = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(EscaType v: t.getRefersToMe()){
				dataTypeSet.add(v.getDataType());
			}
		}
		log.debug("The following datatypes refer to "+resourceType.getLocalName());
		for(String s: dataTypeSet){
			log.debug(s);
		}
	}
	
	private void debugSetOfDirectConnections(Resource resourceType){
		Set<String> dataTypeSet = new HashSet<>();
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			for(EscaType v: t.getLinks().values()){
				dataTypeSet.add(v.getDataType());
			}
		}
		log.debug(resourceType.getLocalName() + " is directly connected to the following types: ");
		for(String s: dataTypeSet){
			log.debug(s);
		}
	}
	
	private void debugReferralTree(Resource resourceType){
		Collection<EscaType> escaResources = escaTypes.getByResourceType(resourceType);
		for(EscaType t: escaResources){
			log.debug(t.getDataType()+" "+t.getName() + " is connected directly to: ");
			for(EscaType c: t.getLinks().values()){
				log.debug(c.getDataType()+ " " + c.getName());
			}
			log.debug("IS REFERED TO BY: " + t.getRefersToMe().size()+ " FROM: ");
			for(EscaType c: t.getRefersToMe()){
				log.debug(c.getDataType()+ " " + c.getName());
			}
		}
	}

	public TopologicalNodes getTopologicalNodes() {
		return topoNodes;
	}
}
