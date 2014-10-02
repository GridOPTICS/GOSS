package pnnl.goss.rdf;

import java.util.HashSet;
import java.util.Set;

import pnnl.goss.rdf.server.Esca60Vocab;

/**
 * For a detailed substation model a TopologicalNode is a set of 
 * connectivity nodes that, in the current network state, are 
 * connected together through any type of closed switches, including 
 * jumpers. Topological nodes changes as the current network state 
 * changes (i.e., switches, breakers, etc. change state). For a 
 * planning model switch statuses are not used to form TopologicalNodes. 
 * Instead they are manually created or deleted in a model builder tool. 
 * TopologialNodes maintained this way are also called 'busses'.
 * 
 * @author D3M614
 *
 */
public class TopologicalNode {
	
	// A volatage level can have multiple topo nodes bue a topo node can only have one voltage level
	
	// For all connectivity nodes 
	
	// one bus per voltage level per substations
	private EscaType substation;
	private EscaType voltageLevel;
	
	public TopologicalNode(EscaType substation, EscaType voltageLevel){
		this.substation = substation;
		this.voltageLevel = voltageLevel;
	}
	
	
	@Override
	public String toString() {
		return "TN: " + substation.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_NAME).toString()+ 
				" vl: " + voltageLevel.getLinkedResourceSingle(Esca60Vocab.BASEVOLTAGE_OBJECT)
										.getLiteralValue(Esca60Vocab.BASEVOLTAGE_NOMINALVOLTAGE)
										.getDouble();
	}
	
	
}
