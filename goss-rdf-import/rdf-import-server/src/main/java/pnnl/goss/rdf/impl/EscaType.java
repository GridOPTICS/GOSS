package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An EscaType is a generic class for containing information about the different
 * rdf classes.  Each EscaType contains a unique mrid.  All {@link Resource}  are
 * defined in {@link Esca60Vocab}.
 * 
 * @author Craig Allwardt
 *
 */
public class EscaType {
	private Resource resource;
	private String dataType;
	private String mrid;
	private static Logger log = LoggerFactory.getLogger(EscaType.class);
	
	/*
	 * Property name-> Literal value (i.e. String, Integer, Float etc) mapping.
	 */
	private Map<String, Literal> literals = new HashMap<>();

	/*
	 * Property name->esca type (links to other Resource types)
	 */
	private Map<String, EscaType> directLinks = new HashMap<>();
	
	/*
	 *  Items that have called the addDirectLink function will be added
	 *  to this set.
	 */
	private Set<EscaType> refersToMe = new HashSet<>();
	
	
	public void addDirectLink(String propertyName, EscaType link){
		if (link != null){
			log.debug("Adding link property: "+propertyName+" to esca obj: "+link.getName());
		}
		else{
			log.debug("Adding null property for: " + propertyName);
		}
		directLinks.put(propertyName, link);
		// TODO Add the "types" to the dataset so that we don't end up with null pointers here.
		if (link != null){
			link.addRefersToMe(this);
		}
	}
	
	public boolean isResourceType(Resource resourceType){
		return dataType.equals(resourceType.getLocalName());
	}
	
	private void addRefersToMe(EscaType refersToMe){
		this.refersToMe.add(refersToMe);
	}
	
	public Map<String, EscaType> getLinks(){
		return directLinks;
	}
	
	public Collection<EscaType> getRefersToMe() {
		return Collections.unmodifiableCollection(refersToMe);		
	}	
	
//	public List<List<EscaType>> getPathToResourceType(Resource resourceType){
//		List<List<EscaType>> paths = new ArrayList<>();
//		
//		for (EscaType t: getLinks().values()){
//			List<EscaType> p = new ArrayList<>();
//			
//			EscaType current = t;
//			p.add(current);
//			
//			while(current != null){
//				
//			}
//			
//		}
//		
//		
//		return paths;
//	}
	
		
	public Literal getLiteralValue(Property property){
		return getLiteralValue(property.getLocalName());
	}
	
	public Literal getLiteralValue(String property){
		if (property.contains(this.dataType)){
			property = property.substring(this.dataType.length()+1);
		}
		return literals.get(property);
	}
	
	public Collection<EscaType> getDirectLinkedResources(Resource resource){
		Set<EscaType> types = new HashSet<EscaType>();
		for(EscaType t: directLinks.values()){
			if (t.getDataType().equals(resource.getLocalName())){
				types.add(t);
			}
		}
		return Collections.unmodifiableCollection(types);
	}
	
	public EscaType getDirectLinkedResourceSingle(Resource resource){
		
		for(EscaType t: directLinks.values()){
			if (t.getDataType().equals(resource.getLocalName())){
				return t;
			}
		}
		return null;
	}
	
	/**
	 * Add literal value to the escatype.  If the key contains the same
	 * datatype as a prefix then that prefix is stripped off and is assumed
	 * to be "contained" as part of the object.
	 * 
	 * @param key
	 * @param value
	 */
	public void addLiteral(String key, Literal value){
		if (key.startsWith(dataType+".")){
			key = key.substring(dataType.length()+1);			
		}
		log.debug("Adding literal key: "+key+" value "+ value+" to datatype: "+dataType);
		literals.put(key,  value);
	}
	
	public Map<String, Literal> getLiterals(){
		return literals;
	}
	
	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @return the mrid
	 */
	public String getMrid() {
		return mrid;
	}
	
	/**
	 * Returns the identifiedobject.name parameter from the rdf code.  If it does not exist
	 * then returns the mrid of the element.
	 * 
	 * @return String
	 */
	public String getName(){
		// Not all of the elements have a name so if they don't then use the mrid
		// as the name until we have something better.
		// TODO find something better that is unique than the mrid
		Literal val = this.literals.get(Esca60Vocab.IDENTIFIEDOBJECT_NAME);
		if (val != null){
			return val.getString();
		}
		return mrid;
	}
	
	public EscaType(EscaType copy){
		this(copy.getResource(), copy.getDataType(), copy.getMrid());
	}

	public EscaType(Resource resource, String dataType, String mrid){
		this.resource = resource;
		this.dataType = dataType;
		this.mrid = mrid;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("------------------------------------------------\n");
		sb.append(this.dataType + " ("+this.mrid+")\n");
		for (String s: literals.keySet()){
			sb.append("\tproperty: "+s+" => "+this.literals.get(s)+"\n");
		}
		sb.append("------------------------------------------------\n\n");
		
		for(EscaType t: directLinks.values()){
			sb.append(this.dataType + " ("+this.mrid+") direct connect to\n");
			sb.append(t);
		}
		
		return sb.toString();
	}
}
