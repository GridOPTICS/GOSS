package pnnl.goss.rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EscaType {
	private Resource resource;
	private String dataType;
	private String mrid;
	private static Logger log = LoggerFactory.getLogger(EscaType.class);
	
	// Property name-> Literal value (i.e. String, Integer, Float etc)
	private Map<String, Literal> literals = new HashMap<>();

	// Property name->esca type
	private Map<String, EscaType> links = new HashMap<>();
	
	public void addLink(String propertyName, EscaType link){
		log.debug("Adding link property: "+propertyName+" to esca obj: "+link);
		links.put(propertyName, link);
	}
	
	public Map<String, EscaType> getLinks(){
		return links;
	}
	
	public Literal getLiteralValue(Property property){
		return getLiteralValue(property.getLocalName());
	}
	
	public Literal getLiteralValue(String property){
		if (property.contains(this.dataType)){
			property = property.substring(this.dataType.length()+1);
		}
		return literals.get(property);
	}
	
	public Collection<EscaType> getLinkedObjectType(Resource resource){
		Set<EscaType> types = new HashSet<EscaType>();
		for(EscaType t: links.values()){
			if (t.getDataType().equals(resource.getLocalName())){
				types.add(t);
			}
		}
		return Collections.unmodifiableCollection(types);
	}
	
	public EscaType getLinkedObjectTypeSingle(Resource resource){
		
		for(EscaType t: links.values()){
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
		
		for(EscaType t: links.values()){
			sb.append(this.dataType + " ("+this.mrid+") direct connect to\n");
			sb.append(t);
		}
		
		return sb.toString();
	}	
	
	private String repeat(String s, int n) {
		if (s == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
}
