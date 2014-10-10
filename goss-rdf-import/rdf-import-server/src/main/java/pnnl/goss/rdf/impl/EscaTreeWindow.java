package pnnl.goss.rdf.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.InvalidArgumentException;
import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class EscaTreeWindow {

	private static Logger log = LoggerFactory.getLogger(EscaTreeWindow.class);
	
	private Model rdfModel;	
	private String dataFilePath;
	private boolean isCimFile;
	private String outputFile;

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

}
