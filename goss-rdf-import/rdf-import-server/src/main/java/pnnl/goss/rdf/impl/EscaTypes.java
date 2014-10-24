package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pnnl.goss.rdf.EscaType;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The EscaTypes class is a container mapping a mrid string to an {@link DefaultEscaType}
 * object.  The map provides convinient functions to return filtered sets of 
 * data.
 * 
 * @author d3m614
 *
 */
public class EscaTypes extends HashMap<String, EscaType> {

	private static final long serialVersionUID = -668345956741148741L;

	/**
	 * Retrieve all instances of a specific resource type from the map.
	 * 
	 * @param subject
	 * @return
	 */
	public Collection<EscaType> getByResourceType(Resource subject){
		return where(subject);
	}
	
	/**
	 * Retrieve all instances of a specific resource type from the map.
	 * 
	 * @see getByResourceType
	 * @param subject
	 * @return
	 */
	public Collection<EscaType> where(Resource resourceType){
		List<EscaType> collection = new ArrayList<>();
		
		for(EscaType t: values()){
			if (t.getDataType().equals(resourceType.getLocalName())){
				collection.add(t);
			}
		}
		
		return Collections.unmodifiableList(collection);
	}
	
	/**
	 * Retrieves the set of datatypes that are in the map.
	 * 
	 * @return
	 */
	public Set<String> types(){
		Set<String> set = new HashSet<>();
		for(EscaType t: values()){
			set.add(t.getDataType());
		}
		return set;
	}
}
