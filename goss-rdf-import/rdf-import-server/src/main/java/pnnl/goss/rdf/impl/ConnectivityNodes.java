package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import pnnl.goss.rdf.EscaType;

public class ConnectivityNodes extends HashSet<ConnectivityNode> {

	private static final long serialVersionUID = 4679801416025585916L;
	
	public Collection<EscaType> toEscaTypeCollection(){
		List<EscaType> types = new ArrayList<>();
		for(ConnectivityNode t: this){
			types.add(t);
		}
		return types;
	}

}
