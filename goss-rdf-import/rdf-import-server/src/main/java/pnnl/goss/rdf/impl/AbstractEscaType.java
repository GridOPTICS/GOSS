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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.Esca60Vocab;

public class AbstractEscaType implements EscaType {
	private static Logger log = LoggerFactory.getLogger(AbstractEscaType.class);
	
	protected Resource resource;
	protected String dataType;
	protected String mrid;
	
	
	/*
	 * Property name-> Literal value (i.e. String, Integer, Float etc) mapping.
	 */
	protected Map<String, Literal> literals = new HashMap<>();

	/*
	 * Property name->esca type (links to other Resource types)
	 */
	protected Map<String, EscaType> directLinks = new HashMap<>();
	
	/*
	 *  Items that have called the addDirectLink function will be added
	 *  to this set.
	 */
	protected Set<EscaType> refersToMe = new HashSet<>();
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#addDirectLink(java.lang.String, pnnl.goss.rdf.EscaType)
	 */
	@Override
	public void addDirectLink(String propertyName, EscaType link) {
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

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#isResourceType(com.hp.hpl.jena.rdf.model.Resource)
	 */
	@Override
	public boolean isResourceType(Resource resourceType){
		return dataType.equals(resourceType.getLocalName());
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getLinks()
	 */
	@Override
	public Map<String, EscaType> getLinks(){
		return directLinks;
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getDirectLinks()
	 */
	@Override
	public Collection<EscaType> getDirectLinks(){
		return Collections.unmodifiableCollection(directLinks.values());
	}
	
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getRefersToMe()
	 */
	@Override
	public Collection<EscaType> getRefersToMe() {
		return Collections.unmodifiableCollection(refersToMe);		
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#addRefersToMe(com.hp.hpl.jena.rdf.model.Resource)
	 */
	@Override
	public void addRefersToMe(EscaType refersToMe){
		this.refersToMe.add(refersToMe);
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getLink(String property)
	 */
	@Override
	public EscaType getLink(Property property){
//		String key = property.getLocalName();
//		if (key.startsWith(dataType+".")){
//			key = key.substring(dataType.length()+1);			
//		}
		
		return directLinks.get(property.getLocalName());
	}	

	@Override
	public boolean hasLiteralProperty(String property) {
		return literals.containsKey(property);
	}

	@Override
	public boolean hasLiteralProperty(Property property) {
		return hasLiteralProperty(property.getLocalName());
	}

	@Override
	public boolean hasDirectLink(Property property) {
		return hasDirectLink(property.getLocalName());
	}

	@Override
	public boolean hasDirectLink(String property) {
		return directLinks.containsKey(property);
	}	

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getRefersToMe(Resource resourceType)
	 */
	public Collection<EscaType> getRefersToMe(Resource resourceType){
		List<EscaType> items = new ArrayList<>();
		for(EscaType t: refersToMe){
			if(t.isResourceType(resourceType)){
				items.add(t);
			}
		}
		return items;
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getLiteralValue(com.hp.hpl.jena.rdf.model.Property)
	 */
	@Override
	public Literal getLiteralValue(Property property){
		return getLiteralValue(property.getLocalName());
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getLiteralValue(java.lang.String)
	 */
	@Override
	public Literal getLiteralValue(String property){
		if (property.contains(this.dataType)){
			property = property.substring(this.dataType.length()+1);
		}
		return literals.get(property);
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getDirectLinkedResources(com.hp.hpl.jena.rdf.model.Resource)
	 */
	@Override
	public Collection<EscaType> getDirectLinkedResources(Resource resource){
		Set<EscaType> types = new HashSet<EscaType>();
		for(EscaType t: directLinks.values()){
			if (t.getDataType().equals(resource.getLocalName())){
				types.add(t);
			}
		}
		return Collections.unmodifiableCollection(types);
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#addLiteral(java.lang.String, com.hp.hpl.jena.rdf.model.Literal)
	 */
	@Override
	public void addLiteral(String key, Literal value){
		log.debug("Adding literal key: "+key+" value "+ value+" to datatype: "+dataType);
		literals.put(key,  value);
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getLiterals()
	 */
	@Override
	public Map<String, Literal> getLiterals(){
		return literals;
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getResource()
	 */
	@Override
	public Resource getResource() {
		return resource;
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getDataType()
	 */
	@Override
	public String getDataType() {
		return dataType;
	}

	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getMrid()
	 */
	@Override
	public String getMrid() {
		return mrid;
	}
	
	/* (non-Javadoc)
	 * @see pnnl.goss.rdf.EscaType#getName()
	 */
	@Override
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

	@Override
	public boolean equals(Object obj) {
		EscaType other = null;
		try{
			other = (EscaType)obj;
		}
		catch(Exception e){
			log.error("Invalid comparison of object");
			return false;
		}
		
		if(other.getMrid().equals(this.mrid)){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return mrid.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<EscaType: "+dataType+ ">"+mrid+"\n");
		sb.append("\tLiterals: ");
		boolean first = true;
		for (String s: literals.keySet()){
			if (first){
				sb.append(s+" => "+this.literals.get(s));
				first= false;
			}
			else{
				sb.append(", "+s+" => "+this.literals.get(s));
			}
		}
		sb.append("\n");
		
		first = true;
		sb.append("\tDirect Links: ");		
		
		for(EscaType t: directLinks.values()){
			if (t == null){
				sb.append("directLink is null!!");
				continue;
			}
			if (first){
				sb.append(t.getDataType() + " => "+t.getMrid());
				first= false;
			}
			else{
				sb.append(", "+t.getDataType() + " => "+t.getMrid()); 
			}
		}
		
		first = true;
		sb.append("\n\tRefers to me: ");		
		
		for(EscaType t: refersToMe){
			if (t == null){
				sb.append("refersToMe value is null!!");
				continue;
			}
			
			if (first){
				sb.append(t.getDataType() + " => "+t.getMrid());
				first= false;
			}
			else {
				sb.append(", "+t.getDataType() + " => "+t.getMrid()); 
			}
		}
		
		return sb.toString();
	}

}
