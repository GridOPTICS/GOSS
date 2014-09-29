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
	private Set<EscaType> children = new HashSet<EscaType>();
	private EscaType parent = null;
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
			
	public int getLevel(){
		if (parent == null){
			return 0;
		}
		else{
			return parent.getLevel() + 1;
		}
	}
	
	public EscaType getParent(){
		return parent;
	}
	
	public void setParent(EscaType parent){
		System.out.println(this.mrid + " parent set to " + parent.mrid);
		this.parent = parent;
	}	
	
	public boolean hasChildren(){
		return this.children.size() > 0;
	}
	
	public void clearChildren(){
		this.children.clear();
	}
	
	public void removeChild(EscaType child){
		this.children.remove(child);
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
	
	public EscaType(EscaType copy){
		this(copy.getResource(), copy.getDataType(), copy.getMrid());
	}

	public EscaType(Resource resource, String dataType, String mrid){
		this.resource = resource;
		this.dataType = dataType;
		this.mrid = mrid;
	}
	
	public void addChild(EscaType link){
		children.add(link);
	}
	
	public Collection<EscaType> getChildren(){
		return children;
	}
	
	public String toString(int level){
		StmtIterator itr = resource.listProperties();
		StringBuilder sb = new StringBuilder();
		String tabs = repeat("\t", level);
		sb.append(tabs + this.dataType + " ("+this.mrid+")\n");
		// Add one tab for the literals.
		tabs = tabs + "\t";
		
		while(itr.hasNext()){
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property prop = stmt.getPredicate();
			if (node.isLiteral()){
				sb.append(tabs + prop.getLocalName()+" = "+node.asLiteral()+"\n");
			}			
		}
		return sb.toString();
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
