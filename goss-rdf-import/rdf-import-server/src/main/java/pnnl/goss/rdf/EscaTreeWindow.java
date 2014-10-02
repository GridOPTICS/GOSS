package pnnl.goss.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.sql.rowset.Predicate;

import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.RDF;

import pnnl.goss.rdf.server.Esca60Vocab;

public class EscaTreeWindow implements KnownTree {

	private static Logger log = LoggerFactory.getLogger(EscaTreeWindow.class);
	
	private Model rdfModel;
	private List<EscaTree> substations;
	private Map<String, EscaTree> mridTreeMap = new HashMap<String, EscaTree>();
	
	private String dataFilePath;
	private boolean isCimFile;
	private String outputFile;
	private HashSet<EscaType> printed = new HashSet<EscaType>();

	/*
	 * A mapping from the mrid to an EscaType.
	 */
	private EscaTypes escaTypeMap = new EscaTypes();
	private Map<String, EscaType> escaTypeSubstationMap = new HashMap<String, EscaType>();
	
	/**
	 * Returns access to an mrid -> EscaType generic mapping
	 * @return
	 */
	public EscaTypes getEscaTypeMap(){
		return escaTypeMap;
	}
	
	/**
	 * Returns access to an mrid -> EscaType substation specific mappings
	 * @return
	 */
	public Map<String, EscaType> getEscaTypeSubstationMap(){
		return escaTypeSubstationMap;
	}
	
	public List<String> getSubstationMrids(){
		List<String> mrids = new ArrayList<String>();
		
		for(String key: escaTypeMap.keySet()){
			if(escaTypeMap.get(key).getDataType().equals("Substation")){
				mrids.add(key);
			}
		}
		
		return mrids;
	}

	/**
	 * Create the application.
	 * 
	 * @throws MalformedURLException
	 * @throws InvalidArgumentException
	 */
	public EscaTreeWindow(String dataFilePath, boolean isCimFile, String outputFile) throws MalformedURLException, InvalidArgumentException {
		this.dataFilePath = dataFilePath;
		this.isCimFile = isCimFile;
		this.outputFile = outputFile;
	}

	/**
	 * Loads all of the subjects into an internal structure that we can use to
	 * find different types of data based upon the mrid.  After this function
	 * is run there should be links from and to all of the different types.
	 */
	public void loadTypeMap() {
		
		// Load all of the subjects into the subject map
		ResIterator resItr = rdfModel.listSubjects();

		while (resItr.hasNext()) {
			Resource res = resItr.nextResource();
			String dataType = getTypeOfSubject(res);
			String mrid = res.getLocalName();
			// System.out.println(mrid+" ("+dataType+")");
			escaTypeMap.put(mrid, new EscaType(res, dataType, mrid));
		}
		
		// Load all of the links between the subjects.
		for (EscaType e: escaTypeMap.values()) {
			loadLiterals(e);
			loadLinks(e);
		}
	}
	
	/**
	 * Load all of the literals into the passed escatype parameter.
	 * 
	 * @param esca
	 */
	private void loadLiterals(EscaType esca){
		StmtIterator itr = esca.getResource().listProperties();
		
		while (itr.hasNext()){
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property prop = stmt.getPredicate();
			
			if (node.isLiteral()){
				esca.addLiteral(prop.getLocalName(), stmt.getLiteral());
				
				//esca.addLiteral(node.get, value);
			}
		}
	}
	
	/**
	 * Loads all of the links to different elements in the graph
	 * 
	 * @param esca
	 */
	private void loadLinks(EscaType esca){
		StmtIterator itr = esca.getResource().listProperties();

		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property prop = stmt.getPredicate();
			if (node.isResource()) {
				Resource res = node.asResource();
				
				// Skip the type because it has already been loaded.
				if (prop.getLocalName().equals("type")){
					log.debug("Skipping type property: "+prop.getLocalName()+" on resource "+res.getLocalName());
				}
				else{
					esca.addDirectLink(prop.getLocalName(), escaTypeMap.get(res.getLocalName()));
				}
			}
		}
	}
	
	
	
	public List<EscaType> getType(String dataType){
		List<EscaType> items = new ArrayList<EscaType>();
		
		for(String k:escaTypeMap.keySet()){
			EscaType type = escaTypeMap.get(k);
			
			if (type.getDataType().equals(dataType)){
				items.add(type);
			}
		}
		
		return items;
	}
	
	
	/**
	 * Loads the esca60 cim xml into memory.
	 * 
	 * @throws InvalidArgumentException
	 */
	public void loadData() throws InvalidArgumentException {
		File file = new File(dataFilePath);
		if (!file.exists()) {
			// throw new
			// InvalidArgumentException("Invalid data file specified!");
			URL url = EscaTreeWindow.class.getClassLoader().getResource(dataFilePath);
			file = new File(url.getPath());
		}

		if (isCimFile) {
			rdfModel = Esca60Vocab.readModel(file.getAbsoluteFile());
		} else {
			throw new InvalidArgumentException("Unsupported file type");
		}
	}

	private List<EscaTree> loadTreeFile(File file) {
		BufferedReader br = null;
		List<EscaTree> substations = new ArrayList<EscaTree>();

		try {

			String sCurrentLine;

			int numTabs = 0;
			EscaTree lastElement = null;

			br = new BufferedReader(new FileReader(file));

			while ((sCurrentLine = br.readLine()) != null) {
				numTabs = sCurrentLine.length();
				sCurrentLine = sCurrentLine.trim();
				numTabs -= sCurrentLine.length();
				sCurrentLine = sCurrentLine.substring(0, sCurrentLine.length() - 1);

				if (numTabs == 0) {
					lastElement = new EscaTree(null, sCurrentLine);
					substations.add(lastElement);
				}
				// Add child of the last element because the tabs are one
				// larger.
				else if (numTabs == lastElement.getLevelsToRoot() + 1) {
					lastElement = new EscaTree(lastElement, sCurrentLine);
				}
				// Sibling of the last element.
				else if (numTabs == lastElement.getLevelsToRoot()) {
					lastElement = new EscaTree(lastElement.getParent(), sCurrentLine);
				} else {
					// Move up the tree to find the parent for this element.
					while (lastElement.getParent().getLevelsToRoot() + 1 > numTabs) {
						lastElement = lastElement.getParent();
					}
					lastElement = new EscaTree(lastElement, sCurrentLine);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return substations;
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

	private String getTypeOfSubject(Resource subject) {
		StmtIterator stmtItr = subject.listProperties();
		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.nextStatement();
			Property pred = stmt.getPredicate();

			if (pred.getLocalName().equals("type")) {

				// System.out.println(stmt.getObject().toString());
				// System.out.println(stmt.getObject().asResource().getLocalName());
				return stmt.getObject().asResource().getLocalName();

			}

		}

		return null;
	}

	private EscaTreeElement buildTreeElement(EscaTreeElement parent, Resource subject, int level) {
		EscaTreeElement current = new EscaTreeElement(subject);
		current.setParent(parent);

		List<Resource> resources = new ArrayList<Resource>();
		StmtIterator itr = subject.listProperties();
		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();
			RDFNode n = stmt.getObject();
			if (n.isResource()) {
				if (!stmt.getPredicate().equals("type")) {
					resources.add(n.asResource());
				}
			}
		}

		for (Resource r : resources) {
			// System.out.println("Building: "+r.toString());
			buildTreeElement(current, r, level + 1);
		}

		return current;
	}

	private void printTree(EscaTreeElement parent, Resource resource, int level) {
		StmtIterator itr = resource.listProperties();
		String tabs = repeat("\t", level);
		String dataType = getTypeOfSubject(resource);
		if (resource.isLiteral()) {
			System.out.println("Literally a " + resource.getLocalName());
		} else {
			System.out.println(tabs + "printingTree: " + resource.getLocalName());
		}
		if (dataType != null) {
			System.out.println(tabs + "Datatype: " + dataType);
		} else {
			System.out.println(tabs + "No Type Sepcifier");
		}
		EscaTreeElement current = new EscaTreeElement(resource);
		current.setParent(parent);
		List<RDFNode> nodes = new ArrayList<RDFNode>();
		List<Property> predicates = new ArrayList<Property>();

		boolean first = true;
		while (itr.hasNext()) {
			Statement stmt = itr.nextStatement();
			RDFNode node = stmt.getObject();
			Property pred = stmt.getPredicate();
			// System.out.println(tabs+"Pred: "+pred.getLocalName());
			if (node.isResource()) {
				// System.out.println(tabs+"Next Subject: "+
				// node.asResource().getLocalName());
				nodes.add(node);
				predicates.add(pred);
				// printTree(node.asResource(), level+1);
			} else if (node.isLiteral()) {
				System.out.println(tabs + "Pred: " + pred.getLocalName() + " Property Value: " + node.asLiteral());
			} else if (node.isAnon()) {
				System.out.println(tabs + "Anon: " + pred.getLocalName());
			}
		}

		for (int i = 0; i < nodes.size(); i++) {
			RDFNode n = nodes.get(i);
			Property p = predicates.get(i);
			if (!p.getLocalName().equals("type")) {
				printTree(current, n.asResource(), level + 1);
			}
		}
	}

	private List<Resource> findElementswithProperty(Property property) {
		NodeIterator itr = rdfModel.listObjectsOfProperty(property);
		List<Resource> resources = new ArrayList<Resource>();
		while (itr.hasNext()) {
			RDFNode node = itr.nextNode();
			resources.add(node.asResource());
		}
		return resources;
	}

	private boolean hasTypePredicate(Resource subject) {
		return subject.hasProperty(Esca60Vocab.TYPE);
	}

	private List<EscaTree> loadCimFile(File file) throws InvalidArgumentException {
		// creates a new, empty in-memory model
		rdfModel = Esca60Vocab.readModel(file.getAbsoluteFile());
		List<Resource> substations = findSubstations();
		String tabs = "";
		List<EscaTree> parents = new ArrayList<EscaTree>();

		ResIterator resItr = rdfModel.listSubjectsWithProperty(Esca60Vocab.TYPE);
		while (resItr.hasNext()) {
			Resource res = resItr.nextResource();
			StmtIterator propItr = res.listProperties();
			System.out.println(res.getURI());
			// while (propItr.hasNext()){
			// Statement stmt = propItr.nextStatement();
			// RDFNode n = stmt.getObject();
			//
			// if(n.)
			// }
		}

		int count = 0;
		List<EscaTreeElement> roots = new ArrayList<EscaTreeElement>();

		for (Resource resource : substations) {
			EscaTreeElement treeRoot = buildTreeElement(null, resource, 0);
			roots.add(treeRoot);

			System.out.println("Mrid: " + treeRoot.getMrid());
			System.out.println("DataType: " + treeRoot.getDataType());
			System.out.println("Name: " + treeRoot.getName());
			System.out.println("Alias: " + treeRoot.getAliasName());
			System.out.println("Path: " + treeRoot.getPath());
			int childCount = 0;

			for (EscaTreeElement child : treeRoot.getChildren()) {
				System.out.println("Count: " + childCount++);
				System.out.println("Mrid: " + child.getMrid());
				System.out.println("DataType: " + child.getDataType());
				System.out.println("Name: " + child.getName());
				System.out.println("Alias: " + child.getAliasName());
				System.out.println("Path: " + treeRoot.getPath());
			}
			// System.out.println("DataType: "+ treeRoot.getDataType());
			// System.out.println("Name: " + treeRoot.getName());
			// System.out.println("Alias: " + treeRoot.getAliasName());
			count++;
			if (count >= 0) {
				break;
			}
			// EscaTree parent = new EscaTree(this, null,
			// Esca60Vocab.SUBSTATION_OBJECT.getLocalName(), mrid, resource);
			// mridTreeMap.put(mrid, parent);
			// //System.out.println(parent);
			// //addChildren(parent);
			// parents.add(parent);
			// parent.addChildren();

		}

		return parents;
	}

	private void printTree(List<EscaTree> roots, Writer writer) {
		for (EscaTree ele : roots) {
			try {
				writer.write(ele.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(ele);
			printTree(ele.getChildren(), writer);
		}
	}

	private List<Resource> findRdfType(Resource resource) {
		List<Resource> resources = new ArrayList<Resource>();
		StmtIterator stmtIter = rdfModel.listStatements(new SimpleSelector(null, RDF.type, resource));
		// StmtIterator stmtIter = rdfModel.listStatements(new
		// SimpleSelector(null, RDF.type, Esca60Vocab.SUBSTATION_OBJECT));

		while (stmtIter.hasNext()) {
			Statement stmt = stmtIter.nextStatement();
			Resource subject = stmt.getSubject();
			resources.add(subject);
		}
		return resources;
	}

	private List<Resource> findSubstations() {
		return findRdfType(Esca60Vocab.TERMINAL_OBJECT); // .SUBSTATION_OBJECT);
		// List<Resource> resources = new ArrayList<Resource>();
		// StmtIterator stmtIter = rdfModel.listStatements(new
		// SimpleSelector(null, RDF.type, Esca60Vocab.SUBSTATION_OBJECT));
		// // StmtIterator stmtIter = rdfModel.listStatements(new
		// // SimpleSelector(null, RDF.type, Esca60Vocab.SUBSTATION_OBJECT));
		//
		// while (stmtIter.hasNext()) {
		// Statement stmt = stmtIter.nextStatement();
		// Resource subject = stmt.getSubject();
		// resources.add(subject);
		// }
		// return resources;
	}

	@Override
	public boolean isKnown(String mrid) {
		return mridTreeMap.containsKey(mrid);
	}

	@Override
	public void addNew(String mrid, EscaTree tree) throws InvalidArgumentException {
		if (isKnown(mrid))
			throw new InvalidArgumentException("Mrid: " + mrid + " is already known!");
		mridTreeMap.put(mrid, tree);

	}

	@Override
	public EscaTree getTree(String mrid) throws InvalidArgumentException {
		if (isKnown(mrid))
			throw new InvalidArgumentException("Mrid: " + mrid + " isn't known!");
		return mridTreeMap.get(mrid);
	}

}
