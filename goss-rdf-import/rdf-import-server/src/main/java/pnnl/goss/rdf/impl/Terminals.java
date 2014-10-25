package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import pnnl.goss.rdf.EscaType;

public class Terminals extends HashSet<Terminal>{

	private static final long serialVersionUID = -5690093079863200972L;
	
	public Collection<EscaType> toEscaTypeCollection(){
		List<EscaType> types = new ArrayList<>();
		for(Terminal t: this){
			types.add(t);
		}
		return types;
	}

}
