package pnnl.goss.rdf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * 
 * @author d3m614
 *
 */
public class Network {
	
	private static Logger log = LoggerFactory.getLogger(Network.class);
	private EscaTypes escaTypes;
	private TopologicalNodes topoNodes = new TopologicalNodes();
	
	public Network(EscaTypes escaTypes){
		log.debug("Creating nework with: " + escaTypes.keySet().size() + " elements.");
		this.escaTypes = escaTypes;
		this.buildTopology();
	}
	
	/**
	 * Notes to myself as building the topology.  
	 * - ConnectivityNodes are directly connected to a VoltageLevel and Substations and 
	 *   referred to by Terminals.
	 * - If a ConnectivityNode is referred to be a Terminal then it will
	 *   be referred to ba at least two Terminals.  
	 * - It seems that if a ConnectivityNode is directly connected to a Substation then
	 *   it will not be referred to by any Terminals.
	 * - It seems that if a ConnectivityNode is directly connected to a VoltageLevel then
	 *   it will be connected to at least two Terminals.
	 */
	private void buildTopology(){
		
		//debugReferralTree(Esca60Vocab.TERMINAL_OBJECT);
		debugSetOfDirectConnections(Esca60Vocab.TERMINAL_OBJECT);
		
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
