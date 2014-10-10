package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Set<EscaType> markedConnectivityNode = new HashSet<>();
	
	public Network(EscaTypes escaTypes){
		log.debug("Creating nework with: " + escaTypes.keySet().size() + " elements.");
		this.escaTypes = escaTypes;
		try {
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
	 * - If a ConnectivityNode is directly connected to a Substation then it is not
	 *   directly connected to a VoltageLevel.
	 * - If a ConnectivityNode is referred to be a Terminal then it will
	 *   be referred to ba at least two Terminals.  
	 * - It seems that if a ConnectivityNode is directly connected to a Substation then
	 *   it will not be referred to by any Terminals.
	 * - It seems that if a ConnectivityNode is directly connected to a VoltageLevel then
	 *   it will be connected to at least two Terminals.
	 * @throws InvalidArgumentException 
	 */
	private void buildTopology() throws InvalidArgumentException{
		
		Collection<EscaType> connectivityNodes = escaTypes.getByResourceType(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
		TopologicalNode currentTopoNode = null;
		Set<EscaType> addedConnectivityNodes = new HashSet<>();
		
		
		for(EscaType connNode: connectivityNodes){
			addConnectivityNode(connNode, addedConnectivityNodes, currentTopoNode);
		}
//		for(EscaType node: connectivityNodes){
//			if ()
//		}
		//debugReferralTree(Esca60Vocab.VOLTAGELEVEL_OBJECT);
		//debugReferralTree(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
		debugReferralTree(Esca60Vocab.BREAKER_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.BREAKER_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.CONFORMLOAD_OBJECT);
		//debugSetOfLiterals(Esca60Vocab.SYNCHRONOUSMACHINE_OBJECT);
		//debugReferralTree(Esca60Vocab.TERMINAL_OBJECT);
		//debugSetOfDirectConnections(Esca60Vocab.TERMINAL_OBJECT);

		//debugSetOfDirectConnections(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
		//debugSetOfReferralConnections(Esca60Vocab.CONNECTIVITYNODE_OBJECT);
	}
	
	private void addConnectivityNode(EscaType connectivityNode, 
										Set<EscaType> addedConnectivityNodes, 
										TopologicalNode currentTopoNode) 
												throws InvalidArgumentException{
		
		if (!addedConnectivityNodes.contains(connectivityNode)){
			// The only item that refers to a connectivity node is a Terminal.
			Collection<EscaType> terminals = connectivityNode.getRefersToMe(); 
			
			if (terminals.size() > 0){
				for(EscaType t: terminals){
					EscaType breaker = t.getDirectLinkedResourceSingle(Esca60Vocab.BREAKER_OBJECT);
					if (breaker != null){
						if (breaker.getLiteralValue(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean() == false){
							if (currentTopoNode == null){
								//log.debug("Creating new topological node");
								currentTopoNode = new TopologicalNode();
								topoNodes.add(currentTopoNode);
							}
							currentTopoNode.addConnectivityNode(connectivityNode);
							addedConnectivityNodes.add(connectivityNode);
						}
//						Collection<EscaType> bTerminals = breaker.getRefersToMe().getDirectLinkedResources(Esca60Vocab.TERMINAL_OBJECT);
//						for(EscaType bt: bTerminals){
//							System.out.println(bt);
//						}
					}
					
					
				}
			}
		}
		else{
			log.debug("Node already accounted for");
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
}
