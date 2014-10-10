package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The EscaTypes class is a container mapping a mrid string to an {@link EscaType}
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
		List<EscaType> collection = new ArrayList<>();
		
		for(EscaType t: values()){
			if (t.getDataType().equals(subject.getLocalName())){
				collection.add(t);
			}
		}
		
		return Collections.unmodifiableList(collection);
	}
	
	public Collection<EscaType> where(Resource resourceType){
		List<EscaType> collection = new ArrayList<>();
		
		for(EscaType t: values()){
			if (t.getDataType().equals(resourceType.getLocalName())){
				collection.add(t);
			}
		}
		
		return Collections.unmodifiableList(collection);
	}
	
}
