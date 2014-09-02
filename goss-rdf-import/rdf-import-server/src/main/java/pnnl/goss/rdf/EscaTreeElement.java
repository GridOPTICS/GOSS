package pnnl.goss.rdf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EscaTreeElement {
	private final String dataType;
	private final String mrid;
	private final Resource subject;
	private Map<Property, String> propertyMap = new HashMap<Property, String>();
	private EscaTreeElement parent;
	private Set<EscaTreeElement> children = new HashSet<EscaTreeElement>();
	
	public Collection<EscaTreeElement> getChildren(){
		return children;
	}
	
	public void setParent(EscaTreeElement parent){
		this.parent = parent;
		if (parent != null){
			parent.addChild(this);
		}
	}
	
	public EscaTreeElement getParent(){
		return this.parent;
	}
	
	private String getTypeOfSubject(){
		StmtIterator stmtItr = subject.listProperties();
		while(stmtItr.hasNext()){
			Statement stmt = stmtItr.nextStatement(); 
			Property pred = stmt.getPredicate();
			
			if (pred.getLocalName().equals("type")){
				
				//System.out.println(stmt.getObject().toString());
				//System.out.println(stmt.getObject().asResource().getLocalName());
				return stmt.getObject().asResource().getLocalName();
				
			}
			
		}
		
		return null;
	}
	
	public void addChild(EscaTreeElement element){
		children.add(element);
	}
	
	public String getMrid(){
		return this.mrid;
	}
	
	public String getDataType(){
		return this.dataType;
	}
	
	public String getPath(){
		return propertyMap.get(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME);
	}
	
	public String getName(){
		return propertyMap.get(Esca60Vocab.IDENTIFIEDOBJECT_NAME);
	}
	
	public String getAliasName(){
		return propertyMap.get(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME);
	}
	
	public Collection<Property> getProperties(){
		return propertyMap.keySet();
	}
	
	
	
	public String getValue(Property key){
		return propertyMap.get(key);
	}
	
	public EscaTreeElement(Resource subject) {
		this.subject = subject;
		this.mrid = this.subject.getLocalName();
		this.dataType = getTypeOfSubject();
		
		StmtIterator itr = this.subject.listProperties();
		//System.out.println("Constructing: "+this.dataType+"("+mrid+")");
				
		while(itr.hasNext()){
			Statement stmt = itr.nextStatement();
			
			//System.out.println("Property: "+stmt.getPredicate());
			propertyMap.put(stmt.getPredicate(), stmt.getObject().toString());
		}
			
	}
}
