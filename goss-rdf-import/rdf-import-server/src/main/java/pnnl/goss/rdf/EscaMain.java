package pnnl.goss.rdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.Substation;
import pnnl.goss.powergrid.topology.nodebreaker.Analog;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimit;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimitSet;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;
import pnnl.goss.powergrid.topology.nodebreaker.ConformLoad;
import pnnl.goss.powergrid.topology.nodebreaker.Disconnector;
import pnnl.goss.powergrid.topology.nodebreaker.Discrete;
import pnnl.goss.powergrid.topology.nodebreaker.Line;
import pnnl.goss.powergrid.topology.nodebreaker.Network;
import pnnl.goss.powergrid.topology.nodebreaker.Terminal;
import pnnl.goss.powergrid.topology.nodebreaker.TopologicalNode;
import pnnl.goss.powergrid.topology.nodebreaker.TransformerWinding;
import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;
import pnnl.goss.rdf.server.BuildPowergrid;
import pnnl.goss.rdf.server.Esca60Vocab;
import pnnl.goss.topology.nodebreaker.dao.BreakerDao;
import pnnl.goss.topology.nodebreaker.dao.NodeBreakerDao;

public class EscaMain {
	
	private static final String PERSISTANCE_UNIT = "nodebreaker_cass_pu";
	
	private static final String ESCA_TEST = "esca60_cim.xml";
	private static boolean bufferedOut = false;
	private static BufferedOutputStream outStream = null;
	
	private static Logger log = LoggerFactory.getLogger(EscaMain.class);
	
	private static void setBufferedOut() throws FileNotFoundException{
		bufferedOut = true;
		File file = new File("c:\\scratch\\rdf_output.txt");
		if (file.exists()){
			file.delete();
		}
		outStream = new BufferedOutputStream(new FileOutputStream(file));
		System.setOut(new PrintStream(outStream));
	}
	
	private static void populateIdentityObjects(EscaType escaType, IdentifiedObject ident){
		Resource resource = escaType.getResource();
		ident.setIdentMrid(escaType.getMrid());
		ident.setIdentDataType(escaType.getDataType());
		
		Statement stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME);
		if (stmt == null){
			log.warn(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME + " was null!");
		}
		else{
			ident.setIdentAlias(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_NAME);
		if (stmt == null){
			log.warn(Esca60Vocab.IDENTIFIEDOBJECT_NAME + " was null!");
		}
		else{
			ident.setIdentName(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME);
		if (stmt == null){
			log.warn(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME + " was null!");
		}
		else{
			ident.setIdentPathName(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION);
		if (stmt == null){
			log.warn(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION + " was null!");
		}
		else{
			ident.setIdentDescription(stmt.getString());
		}		
	}
	
	private static String getPropertyString(Resource resource, Property property){
		if (resource.getProperty(property) != null){
			// Look up the connecting resources mrid.
			if (resource.getProperty(property).getResource() != null){
				return resource.getProperty(property).getResource().getLocalName();
			}
			
			// String literal
			return resource.getProperty(property).getString();
		}
		return null;
	}
	
	private static void storeAnalog(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		Analog entity = new Analog();
		
		entity.setIdentifiedObject(ident);
		
		Resource resource = escaType.getResource();
		
		entity.setNormalValue(resource.getProperty(Esca60Vocab.ANALOG_NORMALVALUE).getDouble());
		entity.setPositiveFlowIn(resource.getProperty(Esca60Vocab.ANALOG_POSITIVEFLOWIN).getBoolean());
		
		dao.persist(entity);
	}
	
	private static void storeAnalogLimitSet(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		AnalogLimitSet entity = new AnalogLimitSet();
		
		entity.setIdentifiedObject(ident);
		
		Resource resource = escaType.getResource();
		
		entity.setLimitSetIsPercentageLimits(resource.getProperty(Esca60Vocab.LIMITSET_ISPERCENTAGELIMITS).getBoolean());
		
		dao.persist(entity);
	}
	
	private static void storeAnalogLimit(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		AnalogLimit entity = new AnalogLimit();
		
		entity.setIdentifiedObject(ident);
		
		Resource resource = escaType.getResource();
		
		entity.setValue(resource.getProperty(Esca60Vocab.ANALOGLIMIT_VALUE).getDouble());
		
		dao.persist(entity);
	}
	
	private static void storeDisconnector(NodeBreakerDao dao, EscaType escaType){
		
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		Disconnector entity = new Disconnector();
		
		entity.setIdentifiedObject(ident);
		
		Resource resource = escaType.getResource();
		
		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
		
		dao.persist(entity);
	}
	
	private static void storeConformLoad(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		ConformLoad entity = new ConformLoad();
		
		entity.setIdentifiedObject(ident);
		
		Resource resource = escaType.getResource();
		
		entity.setEnergyConsumerpFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFEXP).getDouble());
		entity.setEnergyConsumerpfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXED).getDouble());
		entity.setEnergyConsumerpfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXEDPCT).getDouble());
		entity.setEnergyConsumerpVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PVEXP).getDouble());
		
		entity.setEnergyConsumerqFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFEXP).getDouble());
		entity.setEnergyConsumerqfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXED).getDouble());
		entity.setEnergyConsumerqfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXEDPCT).getDouble());
		entity.setEnergyConsumerqVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QVEXP).getDouble());
		
				
		dao.persist(entity);
	}
	
	private static void storeTerminal(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		Terminal entity = new Terminal();
		
		entity.setIdentifiedObject(ident);
				
		dao.persist(entity);
	}
	
	private static void storeDiscrete(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		Discrete entity = new Discrete();
		
		entity.setIdentifiedObject(ident);
				
		dao.persist(entity);
	}
	
	private static void storeSubstation(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		Substation entity = new Substation();
		
		entity.setIdentifiedObject(ident);
				
		dao.persist(entity);
	}
	
	private static void storeVoltageLevel(NodeBreakerDao dao, EscaType escaType){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(escaType, ident);
		
		VoltageLevel entity = new VoltageLevel();
		
		entity.setIdentifiedObject(ident);
				
		dao.persist(entity);
	}
		
	private static void storeLine(NodeBreakerDao dao, EscaType line){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(line, ident);
		
		Line lineObj = new Line();
		
		lineObj.setIdentifiedObject(ident);
		
		Resource resource = line.getResource();
		
		lineObj.setLineRegion(resource.getProperty(Esca60Vocab.LINE_REGION).getResource().getLocalName());
		
		dao.persist(lineObj);
	}
	
	private static void storeBreaker(NodeBreakerDao dao, EscaType breaker){
		
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(breaker, ident);
		
		Breaker entity = new Breaker();
		
		entity.setIdentifiedObject(ident);
		
		
		Resource resource = breaker.getResource();
		
		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
		entity.setRatedCurrent(resource.getProperty(Esca60Vocab.BREAKER_RATEDCURRENT).getDouble());
		
		entity.setMemberOfEquipmentContainer(
				getPropertyString(resource, Esca60Vocab.EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER));
		entity.setConductingEquipmentBaseVoltage(
				getPropertyString(resource, Esca60Vocab.CONDUCTINGEQUIPMENT_BASEVOLTAGE));
		
		dao.persist(entity);
				
		System.out.println("\n");
	}
	
	private static void storeTransformerWinding(NodeBreakerDao dao, EscaType breaker){
		IdentifiedObject ident = new IdentifiedObject();
		
		populateIdentityObjects(breaker, ident);
		
		TransformerWinding entity = new TransformerWinding();
		
		entity.setIdentifiedObject(ident);
				
		Resource resource = breaker.getResource();
		
		entity.setB(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_B).getDouble());
		entity.setG(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_G).getDouble());
		entity.setR(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_R).getDouble());
		entity.setX(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_X).getDouble());
		entity.setRatedU(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDU).getDouble());
		entity.setRatedS(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDS).getDouble());
		
		dao.persist(entity);
	}

	public static void main(String[] args) throws InvalidArgumentException, IOException {
		
		//setBufferedOut();
		EscaTreeWindow window = new EscaTreeWindow(ESCA_TEST, true, "C:\\scratch\\esca_tree.txt");
		window.loadData();
		window.loadTypeMap();
		
		NodeBreakerDao nodeBreakerDao = new NodeBreakerDao(PERSISTANCE_UNIT);
		Map<String, EscaType> typeMap = window.getEscaTypeMap(); 
		
		for (String d : typeMap.keySet()){
			
			String dataType = typeMap.get(d).getDataType();
			
			if(Esca60Vocab.ANALOG_OBJECT.getLocalName().equals(dataType)){
				storeAnalog(nodeBreakerDao, typeMap.get(d));
			}
			else if (Esca60Vocab.ANALOGLIMITSET_OBJECT.getLocalName().equals(dataType)){
				storeAnalogLimitSet(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.ANALOGLIMIT_OBJECT.getLocalName().equals(dataType)){
				storeAnalogLimit(nodeBreakerDao, typeMap.get(d));
			}
			else if (Esca60Vocab.BREAKER_OBJECT.getLocalName().equals(dataType)){
				storeBreaker(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.CONFORMLOAD_OBJECT.getLocalName().equals(dataType)){
				storeConformLoad(nodeBreakerDao, typeMap.get(d));
			}
			else if("Disconnector".equals(dataType)){
				storeDisconnector(nodeBreakerDao, typeMap.get(d));
			}
			else if("Discrete".equals(dataType)){
				storeDiscrete(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.LINE_OBJECT.getLocalName().equals(dataType)){
				storeLine(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.SUBSTATION_OBJECT.getLocalName().equals(dataType)){
				storeSubstation(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.TERMINAL_OBJECT.getLocalName().equals(dataType)){
				storeTerminal(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.TRANSFORMERWINDING_OBJECT.getLocalName().equals(dataType)){
				storeTransformerWinding(nodeBreakerDao, typeMap.get(d));
			}
			else if(Esca60Vocab.VOLTAGELEVEL_OBJECT.getLocalName().equals(dataType)){
				storeVoltageLevel(nodeBreakerDao, typeMap.get(d));
			}
			
			//pnnl.goss.powergrid.topology.Substation
			//System.out.println(d+typeMap.get(d).getDataType());
		}
		
//		List<EscaType> connectivityNodes = window.getType("ConnectivityNode");
//		
//		Network network = new Network();
//		
//
//		for(EscaType cn:connectivityNodes){
//			TopologicalNode topoNode = new TopologicalNode();
//			
//			network.addTopologicalNode(topoNode);
//			
//			for(EscaType type:cn.getChildren()){
//				
//				System.out.println(type);
//			}
//			
//			
//			System.out.println(cn);
//		}
		 
		
		
//		
//		for(String k: typeMap.keySet()){
//			EscaType type = typeMap.get(k);
//			availableTypes.add(type.getDataType());
//		}
//		
//		for(String k: availableTypes){
//			System.out.println(k);
//		}
		
		//if(true)return;
		
//		for(String key:typeMap.keySet()){
//			System.out.println(typeMap.get(key).getDataType());
//		}
		// This is puzzling the way I have configured this.
		//window.loadSubjectTree("_7138742088364182230");
		
		//window.invertFromLevel(Esca60Vocab.SUBSTATION_OBJECT);
		// A Substation
		//BuildPowergrid grid = new BuildPowergrid();
		//grid.buildPowergrid(window.getEscaTypeMap(), window.getEscaTypeSubstationMap());
		
		// A Substation
		//window.printInvertedTree("_7138742088364182230");

		
		// A breaker
		//window.printInvertedTree("_6086371616589253666");
		// A VoltageLevel
		//window.printInvertedTree("_7385660062756494042");
		
		
		//window.printTree("_7138742088364182230");
		// A Terminal
		//window.printTree("_2463136265274055557");
		if (bufferedOut){
			outStream.flush();
		}
		//window.printSubstations();
		
		//window.printTerminalTree("_1859399559611018070");
		//EscaTreeWindow window = new EscaTreeWindow("C:\\scratch\\esca_tree.txt", false, "C:\\scratch\\esca_tree_out.txt");
	}

}
