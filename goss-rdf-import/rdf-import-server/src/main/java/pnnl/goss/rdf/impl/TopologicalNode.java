package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.InvalidArgumentException;
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
 * @author Craig Allwardt
 *
 */
public class TopologicalNode {
	
	private static Logger log = LoggerFactory.getLogger(TopologicalNode.class);
	
	// A volatage level can have multiple topo nodes bue a topo node can only have one voltage level
	
	// For all connectivity nodes 
	
	// one bus per voltage level per substations
	private EscaType substation;
	private EscaType voltageLevel;
	private ConnectivityNodes connectivityNodes = new ConnectivityNodes();
	private Terminals terminals = new Terminals();
	private String identifier;
	private EscaTypeBag loads;
	private EscaTypeBag shunts;
	private EscaTypeBag generators;
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public TopologicalNode(){
		
	}
	
	public EscaType getVoltageLevelRes(){
		for(ConnectivityNode cn: connectivityNodes){
			EscaType vl = cn.getVoltageLevelRes();
			if (vl != null){
				if (voltageLevel == null){
					voltageLevel = vl;
				}
				else{
					if (vl != voltageLevel){
						log.error("Multiple voltage levels for a node!");
					}
				}
			}
		}
		return voltageLevel;
	}
	
	public void addConnectivityNode(ConnectivityNode node){
		connectivityNodes.add(node);
		for (Terminal t: node.getTerminals()){
			addTerminal(t);
		}
	}
	
	public void addTerminal(Terminal terminal) {
		terminals.add(terminal);
	}
	
	
	public EscaType getSubstation() {
		return substation;
	}


	public EscaType getVoltageLevel() {
		return voltageLevel;
	}
	
	public Terminals getTerminals() {
		return terminals;
	}

	public ConnectivityNodes getConnectivityNodes() {
		return connectivityNodes;
	}
	

	@Override
	public String toString() {
		return "TN: " + this.identifier + " #connectivity nodes: "+ this.connectivityNodes.size() + " #terminals: "+ this.terminals.size();
//		return "TN: " + substation.getLiteralValue(Esca60Vocab.IDENTIFIEDOBJECT_NAME).toString()+ 
//				" vl: " + voltageLevel.getDirectLinkedResourceSingle(Esca60Vocab.BASEVOLTAGE_OBJECT)
//										.getLiteralValue(Esca60Vocab.BASEVOLTAGE_NOMINALVOLTAGE)
//										.getDouble();
	}
	
	
}
