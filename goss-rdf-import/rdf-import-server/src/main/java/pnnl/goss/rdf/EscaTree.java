package pnnl.goss.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class EscaTree {
	private EscaTree parent = null;
	private String dataType = null;
	private String mrid = null;
	private Resource subject = null;
	private List<EscaTree> children = new ArrayList<EscaTree>();
	private Map<String, String> propertyValues = new HashMap<String, String>();
	private KnownTree knownTree;
	
	/**
	 * @return the parent
	 */
	public EscaTree getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(EscaTree parent) {
		this.parent = parent;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the mrid
	 */
	public String getMrid() {
		return mrid;
	}

	/**
	 * @param mrid the mrid to set
	 */
	public void setMrid(String mrid) {
		this.mrid = mrid;
	}
	
	
	public EscaTree(EscaTree parent, String parsableData){
		this.parent = parent;
		if(this.parent != null){
			this.parent.addChild(this);
		}
		String tokens[] = parsableData.split("\\(");
		// Should have 2
		assert(tokens.length == 2);
		String dataTypeAndMrid[] = tokens[0].split("\\->");
		assert(dataTypeAndMrid.length == 2);
		this.dataType = dataTypeAndMrid[0];
		this.mrid = dataTypeAndMrid[1];
		String propertyAndValues[] = tokens[1].split(", ");
		for(String kv :propertyAndValues){
			String kAndV[] = kv.split("=");
			assert(kAndV.length == 2);
			this.setPropertyValue(kAndV[0], kAndV[1]);
		}
	}

	public EscaTree(KnownTree knownCallback, EscaTree parent, String dataType, String mrid, Resource subject) throws InvalidArgumentException{
		if(dataType == null || dataType.isEmpty()) throw new InvalidArgumentException("dataType");
		if(mrid == null || mrid.isEmpty()) throw new InvalidArgumentException("mrid");
		
		this.parent = parent;
		this.dataType = dataType;
		this.mrid = mrid;
		this.subject = subject;
		this.knownTree = knownCallback;
		knownCallback.addNew(mrid, this);
	}
	
	
	
	private String getDataType(Resource resource){
		StmtIterator stmt = resource.listProperties();
		
		while(stmt.hasNext()){
			Statement statement = stmt.nextStatement();
			RDFNode node = statement.getObject();
			Resource sub = statement.getSubject();
			Property pred = statement.getPredicate();
			if (node.isURIResource()){
				System.out.println("Yes is urlresource");
			}
			System.out.println("Subject is: " + sub.getLocalName());
			
			if(node.isLiteral()){
				System.out.println("Literal: "+node.asLiteral());
			}
			else if(node.isResource()){
				System.out.println("Resource: "+node.asResource().getLocalName());
			}
		}
		return resource.getProperty(Esca60Vocab.TYPE).getString();
		//Statement stmt = statement.getStatementProperty(Esca60Vocab.TYPE);
		//return stmt.getPredicate().getLocalName();
		
	}
	
	
	public void addChildren(){
		StmtIterator itr = subject.listProperties();
		System.out.println("Subject: " + this.subject.getLocalName());
		
		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();

			RDFNode node = stmt.getObject();
			Property property = stmt.getPredicate(); 
			System.out.println("Property: " + property.getLocalName());
			
			// If resource then add new tree level else add property as literal value.
			if (node.isResource()){
				String mrid = node.asResource().getLocalName();
				String dataType = node.asResource().getLocalName();
				System.out.println("Node: " + node.asResource().getLocalName());
				
				if(knownTree.isKnown(mrid)){
					try {
						addChild(knownTree.getTree(mrid));
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					try {
						EscaTree tree = new EscaTree(knownTree, this, getDataType(node.asResource()), mrid, node.asResource()); //, this.subject);
						tree.addChildren();
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Object Resource: " + node.toString() + " Local name: " + node.asResource().getLocalName()); //.asNode().getLocalName());
			}
			else{
				System.out.println("Add literal properties here.");
				System.out.println("\tObject Literal: " + node.toString() + " Local name: " + node.asLiteral()); //.asNode().getLocalName());
			}
			
			//String nodeName = getNodeName(stmt);
						
		}
		System.out.println("done");
	}
	
	public void addChild(EscaTree child){
		children.add(child);
	}
	
	public List<EscaTree> getChildren(){
		return children;
	}
	
	public void printChildrenOfSubject(){
		
		StmtIterator itr = subject.listProperties();
	
		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();
			Resource subject = stmt.getSubject();
			RDFNode node = stmt.getObject();
			Property property = stmt.getPredicate();
			System.out.println("Subject: " + subject.getLocalName()); 
			System.out.println("Property: " + property.getLocalName());
			if (node.isResource()){
				System.out.println("Object Resource: " + node.toString() + " Local name: " + node.asResource().getLocalName()); //.asNode().getLocalName());
			}
			else{
				System.out.println("Object Literal: " + node.toString() + " Local name: " + node.asLiteral()); //.asNode().getLocalName());
			}
			
			//String nodeName = getNodeName(stmt);
						
		}
		System.out.println("done");
	}
	
	public Resource getSubject(){
		return subject;
	}
	
	public boolean hasChildren(){
		return children.size() > 0;
	}
	
	public void setPropertyValue(String property, String value){
		propertyValues.put(property, value);
	}
	
	public String getPropertyValue(String property){
		return propertyValues.get(property);
	}
	
	public int getLevelsToRoot(){
		int levels = 0;
		EscaTree parentLevel = this.parent;
		while(parentLevel != null){
			levels += 1;
			parentLevel = parentLevel.getParent();
		}
		return levels;
	}
	
	public String repeat(String s, int n) {
	    if(s == null) {
	        return null;
	    }
	    final StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < n; i++) {
	        sb.append(s);
	    }
	    return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(repeat("\t",  getLevelsToRoot()));
		sb.append(this.dataType);
		sb.append("->");
		sb.append(this.mrid);
		sb.append("(");
		boolean first = true;
		for(Map.Entry<String, String> entry: this.propertyValues.entrySet()){
			if (!first){
				sb.append(", ");
			}
			first = false;
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());				
		}
		sb.append(")");
		return sb.toString(); //repeat("\t",  getLevelsToRoot()) + this.dataType + "->" + this.mrid;
	}
	
}
