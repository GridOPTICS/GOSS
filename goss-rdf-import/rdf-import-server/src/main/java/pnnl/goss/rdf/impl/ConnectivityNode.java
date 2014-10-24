package pnnl.goss.rdf.impl;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.Esca60Vocab;

public class ConnectivityNode extends AbstractEscaType {

	private Terminals terminals;
	
	/**
	 * Lazy load terminals that are connected to this node.
	 * 
	 * @return set of terminals connected to this node.
	 */
	public Terminals getTerminals(){
		if(terminals == null){
			terminals = new Terminals();
			for(EscaType t: this.getRefersToMe(Esca60Vocab.TERMINAL_OBJECT)) {
				terminals.add((Terminal)t);
			}
		}
		
		return terminals;
	}
}
