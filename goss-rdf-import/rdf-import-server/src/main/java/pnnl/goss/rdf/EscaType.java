package pnnl.goss.rdf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EscaType {
	Resource resource;
	String dataType;
	String mrid;
	Set<EscaType> children = new HashSet<EscaType>();
	EscaType parent = null;
	
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
		return toString(getLevel());
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
