package pnnl.goss.rdf.impl;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.Esca60Vocab;

public class Terminal extends AbstractEscaType {
		
	public EscaType getEquipment(){
		return getLink(Esca60Vocab.TERMINAL_CONDUCTINGEQUIPMENT);
	}
	
}
