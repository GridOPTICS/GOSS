package pnnl.goss.rdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import pnnl.goss.powergrid.topology.Substation;
import pnnl.goss.powergrid.topology.nodebreaker.ACLineSegment;
import pnnl.goss.powergrid.topology.nodebreaker.Accumulator;
import pnnl.goss.powergrid.topology.nodebreaker.Analog;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimit;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimitSet;
import pnnl.goss.powergrid.topology.nodebreaker.BaseVoltage;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;
import pnnl.goss.powergrid.topology.nodebreaker.BusBarSection;
import pnnl.goss.powergrid.topology.nodebreaker.ConformLoad;
import pnnl.goss.powergrid.topology.nodebreaker.ConformLoadGroup;
import pnnl.goss.powergrid.topology.nodebreaker.ConformLoadSchedule;
import pnnl.goss.powergrid.topology.nodebreaker.ConnectivityNode;
import pnnl.goss.powergrid.topology.nodebreaker.CurveData;
import pnnl.goss.powergrid.topology.nodebreaker.EquipmentContainer;
import pnnl.goss.powergrid.topology.nodebreaker.GeographicalRegion;
import pnnl.goss.powergrid.topology.nodebreaker.Line;
import pnnl.goss.powergrid.topology.nodebreaker.SubGeographicalRegion;
import pnnl.goss.powergrid.topology.nodebreaker.SynchronousMachine;
import pnnl.goss.powergrid.topology.nodebreaker.Terminal;
import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;
import pnnl.goss.rdf.server.Esca60Vocab;
import pnnl.goss.topology.nodebreaker.dao.NodeBreakerDao;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EscaMain {
	
	private static final String PERSISTANCE_UNIT = "nodebreaker_cass_pu";
	
	private static final String ESCA_TEST = "esca60_cim.xml";
	private static boolean bufferedOut = false;
	private static BufferedOutputStream outStream = null;
	
	private static Logger log = LoggerFactory.getLogger(EscaMain.class);
	
	/**
	 * A mapping of all of the identified resources that have been managed by
	 * the system.
	 */
	private static Map<String, NodeBreakerDataType> identifiedMap = new HashMap<>();
	
	/**
	 * A loaded mapping from mrid to escatype which is loaded from the cim model
	 * file.
	 */
	private static Map<String, EscaType> typeMap = null;
	
	private static NodeBreakerDao nodeBreakerDao = new NodeBreakerDao(PERSISTANCE_UNIT);
	
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
		ident.setMrid(escaType.getMrid());
		//ident.setDataType(escaType.getDataType());
		
		Statement stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME);
		if (stmt == null){
			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_ALIASNAME + " was null!");
		}
		else{
			ident.setAlias(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_NAME);
		if (stmt == null){
			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_NAME + " was null!");
		}
		else{
			ident.setName(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME);
		if (stmt == null){
			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_PATHNAME + " was null!");
		}
		else{
			ident.setPath(stmt.getString());
		}
		stmt = resource.getProperty(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION);
		if (stmt == null){
			//log.warn(Esca60Vocab.IDENTIFIEDOBJECT_DESCRIPTION + " was null!");
		}
		else{
			ident.setDescription(stmt.getString());
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
	
	private static String getPropertyString(Resource resource, String property){
		
		StmtIterator stmts = resource.listProperties();
		
		while(stmts.hasNext()){
			Statement stmt = stmts.next();
			
			Resource pred = stmt.getPredicate();
			// If the resource matches then the caller is expecting the mrid of the
			if (pred.isResource()){
				if (pred.getLocalName().equals(property)){
					RDFNode node = stmt.getObject();
					return node.asResource().getLocalName();
				}
			}
			else if(stmt.getPredicate().isProperty()){
				
			}
			else if(stmt.getPredicate().isLiteral()){
				
			}
		}
		
		
		return null;
	}
//	
//	private static void storeAnalog(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		Analog entity = new Analog();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setNormalValue(resource.getProperty(Esca60Vocab.ANALOG_NORMALVALUE).getDouble());
//		entity.setPositiveFlowIn(resource.getProperty(Esca60Vocab.ANALOG_POSITIVEFLOWIN).getBoolean());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeAnalogLimitSet(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		AnalogLimitSet entity = new AnalogLimitSet();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setLimitSetIsPercentageLimits(resource.getProperty(Esca60Vocab.LIMITSET_ISPERCENTAGELIMITS).getBoolean());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeAnalogLimit(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		AnalogLimit entity = new AnalogLimit();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setValue(resource.getProperty(Esca60Vocab.ANALOGLIMIT_VALUE).getDouble());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeConnectivityNode(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		ConnectivityNode entity = new ConnectivityNode();
//		
//		entity.setIdentifiedObject(ident);
//		
//		dao.persist(entity);
//	}
//	
//	private static void storePowerTransformer(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		PowerTransformer entity = new PowerTransformer();
//		
//		entity.setIdentifiedObject(ident);
//		
//		dao.persist(entity);
//	}
//	
//	public static void storeBusBarSection(NodeBreakerDao dao, EscaType escaType){
//		
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		BusbarSection entity = new BusbarSection();
//		
//		entity.setIdentifiedObject(ident);
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeDisconnector(NodeBreakerDao dao, EscaType escaType){
//		
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		Disconnector entity = new Disconnector();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeConformLoad(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		ConformLoad entity = new ConformLoad();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setEnergyConsumerpFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFEXP).getDouble());
//		entity.setEnergyConsumerpfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXED).getDouble());
//		entity.setEnergyConsumerpfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PFIXEDPCT).getDouble());
//		entity.setEnergyConsumerpVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_PVEXP).getDouble());
//		
//		entity.setEnergyConsumerqFexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFEXP).getDouble());
//		entity.setEnergyConsumerqfixed(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXED).getDouble());
//		entity.setEnergyConsumerqfixedPct(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QFIXEDPCT).getDouble());
//		entity.setEnergyConsumerqVexp(resource.getProperty(Esca60Vocab.ENERGYCONSUMER_QVEXP).getDouble());
//		
//				
//		dao.persist(entity);
//	}
//	
//	private static void storeTerminal(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		Terminal entity = new Terminal();
//		
//		entity.setIdentifiedObject(ident);
//				
//		dao.persist(entity);
//	}
//	
//	private static void storeDiscrete(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		Discrete entity = new Discrete();
//		
//		entity.setIdentifiedObject(ident);
//				
//		dao.persist(entity);
//	}
//	
//	private static Substation storeSubstation(NodeBreakerDao dao, EscaType escaType){
//		Substation entity = new Substation();
//				
//		populateIdentityObjects(escaType, entity);
//						
//		dao.persist(entity);
//		
//		return entity;
//	}
//	
//	private static VoltageLevel storeVoltageLevel(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		VoltageLevel entity = new VoltageLevel();
//		
//		entity.setIdentifiedObject(ident);
//				
//		dao.persist(entity);
//		
//		return entity;
//	}
//		
//	private static void storeLine(NodeBreakerDao dao, EscaType line){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(line, ident);
//		
//		Line lineObj = new Line();
//		
//		lineObj.setIdentifiedObject(ident);
//		
//		Resource resource = line.getResource();
//		
//		lineObj.setLineRegion(resource.getProperty(Esca60Vocab.LINE_REGION).getResource().getLocalName());
//		
//		dao.persist(lineObj);
//	}
//	
//	private static void storeCurveData(NodeBreakerDao dao, EscaType escaType){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(escaType, ident);
//		
//		CurveData entity = new CurveData();
//		
//		entity.setIdentifiedObject(ident);
//		
//		Resource resource = escaType.getResource();
//		
//		entity.setXvalue(resource.getProperty(Esca60Vocab.CURVEDATA_XVALUE).getDouble());
//		entity.setY1value(resource.getProperty(Esca60Vocab.CURVEDATA_Y1VALUE).getDouble());
//		entity.setY2value(resource.getProperty(Esca60Vocab.CURVEDATA_Y2VALUE).getDouble());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeTapChanger(NodeBreakerDao dao, EscaType breaker){
//		
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(breaker, ident);
//		
//		TapChanger entity = new TapChanger();
//		
//		entity.setIdentifiedObject(ident);
//				
//		Resource resource = breaker.getResource();
//		
//		entity.setHighStep(resource.getProperty(Esca60Vocab.TAPCHANGER_HIGHSTEP).getInt());
//		entity.setNormalStep(resource.getProperty(Esca60Vocab.TAPCHANGER_NORMALSTEP).getInt());
//		entity.setLowStep(resource.getProperty(Esca60Vocab.TAPCHANGER_LOWSTEP).getInt());
//		entity.setNeutralStep(resource.getProperty(Esca60Vocab.TAPCHANGER_NEUTRALSTEP).getInt());
//		entity.setTculControlMode(resource.getProperty(Esca60Vocab.TAPCHANGER_TCULCONTROLMODE).getResource().getLocalName());
//		Statement stmt = resource.getProperty(Esca60Vocab.TAPCHANGER_STEPVOLTAGEINCREMENT);
//		if (stmt != null){
//			entity.setStepVoltageIncrement(stmt.getDouble());
//		}
//		entity.setTransformerWinding(resource.getProperty(Esca60Vocab.TAPCHANGER_TRANSFORMERWINDING).getResource().getLocalName());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeRegularTimePoint(NodeBreakerDao dao, EscaType breaker){
//		
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(breaker, ident);
//		
//		RegularTimePoint entity = new RegularTimePoint();
//		
//		entity.setIdentifiedObject(ident);
//				
//		Resource resource = breaker.getResource();
//		
//		Statement stmt = resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_INTERVALSCHEDULE);
//		if (stmt != null && stmt.getResource() != null){
//			entity.setIntervalSchedule(stmt.getResource().getLocalName());
//		}
//		
//		entity.setValue1(resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_VALUE1).getDouble());
//		entity.setValue2(resource.getProperty(Esca60Vocab.REGULARTIMEPOINT_VALUE2).getDouble());
//		
//		dao.persist(entity);
//	}
//	
//	private static void storeBreaker(NodeBreakerDao dao, EscaType breaker){
//		
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(breaker, ident);
//		
//		Breaker entity = new Breaker();
//		
//		entity.setIdentifiedObject(ident);
//		
//		
//		Resource resource = breaker.getResource();
//		
//		entity.setSwitchNormalOpen(resource.getProperty(Esca60Vocab.SWITCH_NORMALOPEN).getBoolean());
//		entity.setRatedCurrent(resource.getProperty(Esca60Vocab.BREAKER_RATEDCURRENT).getDouble());
//		
//		entity.setMemberOfEquipmentContainer(
//				getPropertyString(resource, Esca60Vocab.EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER));
//		entity.setConductingEquipmentBaseVoltage(
//				getPropertyString(resource, Esca60Vocab.CONDUCTINGEQUIPMENT_BASEVOLTAGE));
//		
//		dao.persist(entity);
//				
//		System.out.println("\n");
//	}
//	
//	private static void storeTransformerWinding(NodeBreakerDao dao, EscaType breaker){
//		IdentifiedObject ident = new IdentifiedObject();
//		
//		populateIdentityObjects(breaker, ident);
//		
//		TransformerWinding entity = new TransformerWinding();
//		
//		entity.setIdentifiedObject(ident);
//				
//		Resource resource = breaker.getResource();
//		
//		entity.setB(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_B).getDouble());
//		entity.setG(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_G).getDouble());
//		entity.setR(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_R).getDouble());
//		entity.setX(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_X).getDouble());
//		entity.setRatedU(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDU).getDouble());
//		entity.setRatedS(resource.getProperty(Esca60Vocab.TRANSFORMERWINDING_RATEDS).getDouble());
//		
//		dao.persist(entity);
//	}
//		
//	private static Object buildIdentified(Class cls, EscaType escaType){
//		
//		Identified identified = null;
//		try {
//			IdentifiedObject obj = new IdentifiedObject();
//			populateIdentityObjects(escaType, obj);
//			identified = (Identified) cls.newInstance();
//			identified.setIdentifiedObject(obj);		
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return identified;
//	}
	
	public static SynchronousMachine createSynchronousMachine(EscaType escaType){
		SynchronousMachine s = new SynchronousMachine();
		populateIdentityObjects(escaType, s);
		return s;
	}
	
	public static Substation createSubstation(EscaType escaType){
		Substation s = new Substation();
		populateIdentityObjects(escaType, s);
		return s;
	}
	
	public static Terminal createTerminal(EscaType escaType){
		Terminal t = new Terminal();
		populateIdentityObjects(escaType, t);
		return t;
	}
	
	public static VoltageLevel createVoltageLevel(EscaType escaType){
		VoltageLevel e = new VoltageLevel();
		populateIdentityObjects(escaType, e);		
		return e;
	}
	
	public static GeographicalRegion createGeographicRegion(EscaType escaType){
		GeographicalRegion g = new GeographicalRegion();
		populateIdentityObjects(escaType, g);
		return g;
	}
	
	public static void populateGeographicRegions(){
		for (String d : typeMap.keySet()){
			EscaType escaType = typeMap.get(d);
			String dataType = escaType.getDataType();
			NodeBreakerDataType entity = null;
			
			if ("GeographicalRegion".equals(dataType)){
				entity = createGeographicRegion(escaType);
			}
			
			if(entity != null){
				identifiedMap.put(((IdentifiedObject)entity).getMrid(), entity);
				nodeBreakerDao.persist(entity);
			}
		}
	}
		
	public static String populateDataType(Class klass, String escaDataType){
		int countAdded = 0;
		for (String d : typeMap.keySet()){
			EscaType escaType = typeMap.get(d);
			String dataType = escaType.getDataType();
			NodeBreakerDataType entity = null;
			
			if (escaDataType.equals(dataType)){
				try {
					entity = (NodeBreakerDataType)klass.newInstance();
					populateIdentityObjects(escaType, (IdentifiedObject)entity);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// createGeographicRegion(escaType);
			}
			
			if(entity != null){
				identifiedMap.put(((IdentifiedObject)entity).getMrid(), entity);
				nodeBreakerDao.persist(entity);
				countAdded+=1;
			}
		}
		
		return "Added: "+countAdded+ " " + escaDataType;
	}
	
	public static void printIdentifiedMap(){
		for (String d : identifiedMap.keySet()){
			NodeBreakerDataType obj = identifiedMap.get(d);
			System.out.println("Type: "+obj.getDataType()+"\n\t"+obj.toString());
		}
	}
	
		
	public static void addToParent(String parentMrid, String methodName, Object objectToAdd){
		
		Object parent = identifiedMap.get(parentMrid);
		try {
			Method method = parent.getClass().getMethod(methodName, objectToAdd.getClass());
			method.invoke(method,  objectToAdd);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Call a method on the passed mrid referenced objects.  The sinkMrid object will
	 * be looked up from the  map.  The sink object will have its methodName called.
	 * The methodName will be passed the sourceMrid's object.  
	 * 
	 * In addition if the source's main interface is not the interface that is
	 * used as the parameter then the code will search the other interfaces before
	 * raising an error.
	 * 
	 * @param sinkMrid
	 * @param sourceMrid
	 * @param methodName
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void callMethod(String sinkMrid, String sourceMrid, String methodName){
		Object sinkObj = identifiedMap.get(sinkMrid);
		Object sourceObj = identifiedMap.get(sourceMrid);
		
		
		Class sinkClass = sinkObj.getClass();
		Class sourceClass = sourceObj.getClass();
		Method method=null;
		try {
			method = sinkClass.getMethod(methodName, sourceClass);
			method.invoke(sinkObj, sourceObj);
		}
		catch (NoSuchMethodException e){
			boolean success = false;
			for(Class c : sourceClass.getInterfaces()){
				try{
					sourceClass = c;
					method = sinkClass.getMethod(methodName, sourceClass);
					method.invoke(sinkObj, sourceObj);
					success = true;
				}
				catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					
				}
				if (success){
					break;
				}
			}			
			if (!success){
				System.out.println("No method takes any of the source interfaces as an argument.");
				e.printStackTrace();
			}
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Call a on sink and pass it an instance of the sourceClass.
	 * 
	 * @param sinkClass
	 * @param sourceClass
	 * @param escaType
	 * @param escaPropertyName
	 * @param methodName
	 */
	@SuppressWarnings("rawtypes")
	public static void addSingularRelation(Class sinkClass, Class sourceClass, 
			String escaType, String escaPropertyName, String methodName){
		
		for(EscaType t: typeMap.values()){
			// escaType is the datatype associated with the child (nodebreaker model) class
			// and the escaPropertyName should be a reference to the parent (nodebreaker model)
			// object.
			if (t.getDataType().equals(escaType)){
				
				String sinkMrid = t.getMrid();
				String sourceMrid = getPropertyString(t.getResource(), escaPropertyName);
				
				callMethod(sinkMrid, sourceMrid, methodName);
				
				nodeBreakerDao.persist(identifiedMap.get(sinkMrid));				
			}
		}
		
		
	}
	
	
		
	/**
	 * Add relations from and to parent and child using the passed child escaType and 
	 * escaPropertyName as the linking mechanism.
	 * 
	 * The child class is expected to have a set<parenttype> method.
	 * The parent class is expected to have an add<childtype> method.
	 * 
	 * The child and parent class types are determined by class.getSimpleType().
	 * 
	 * @param child
	 * @param parent
	 * @param escaType
	 * @param escaPropertyName
	 */
	@SuppressWarnings("rawtypes")
	public static void addRelation(Class child, Class parent, String escaType, String escaPropertyName){
				
		for(EscaType t: typeMap.values()){
			// escaType is the datatype associated with the child (nodebreaker model) class
			// and the escaPropertyName should be a reference to the parent (nodebreaker model)
			// object.
			if (t.getDataType().equals(escaType)){
				
				String mridChild = t.getMrid();
				String mridParent = getPropertyString(t.getResource(), escaPropertyName);
				
				// Assumption is that child will have a set<parentclassname> method and the
				// child will have an add<parentclassname> method.
				callMethod(mridChild, mridParent, "set"+parent.getSimpleName());
				callMethod(mridParent, mridChild, "add"+child.getSimpleName());
				
				nodeBreakerDao.persist(identifiedMap.get(mridParent));				
			}
		}
		
		
	}
	

	public static void main(String[] args) throws InvalidArgumentException, IOException {
		
		//setBufferedOut();
		EscaTreeWindow window = new EscaTreeWindow(ESCA_TEST, true, "C:\\scratch\\esca_tree.txt");
		window.loadData();
		window.loadTypeMap();
		
		typeMap = window.getEscaTypeMap();
		
		System.out.println(populateDataType(GeographicalRegion.class, "GeographicalRegion"));
		System.out.println(populateDataType(SubGeographicalRegion.class, "SubGeographicalRegion"));
		
		
		System.out.println(populateDataType(Accumulator.class, "Accumulator"));
		System.out.println(populateDataType(ACLineSegment.class, "ACLineSegment"));
		System.out.println(populateDataType(Analog.class, "Analog"));
		System.out.println(populateDataType(AnalogLimit.class, "AnalogLimit"));
		System.out.println(populateDataType(AnalogLimitSet.class, "AnalogLimitSet"));
		
		System.out.println(populateDataType(BaseVoltage.class, "BaseVoltage"));
		System.out.println(populateDataType(Breaker.class, "Breaker"));
		System.out.println(populateDataType(BusBarSection.class, "BusbarSection"));
		
		System.out.println(populateDataType(ConformLoad.class, "ConformLoad"));
		System.out.println(populateDataType(ConformLoadGroup.class, "ConformLoadGroup"));
		System.out.println(populateDataType(ConformLoadSchedule.class, "ConformLoadSchedule"));
		System.out.println(populateDataType(ConnectivityNode.class, "ConnectivityNode"));
		System.out.println(populateDataType(CurveData.class, "CurveData"));
			
		
		System.out.println(populateDataType(Line.class, "Line"));
		System.out.println(populateDataType(Substation.class, "Substation"));
		
		System.out.println(populateDataType(Terminal.class, "Terminal"));
		System.out.println(populateDataType(Breaker.class, "Breaker"));
		
		// Add parent child relationships.
		addRelation(SubGeographicalRegion.class, GeographicalRegion.class, "SubGeographicalRegion", "SubGeographicalRegion.Region");
		addRelation(Line.class, SubGeographicalRegion.class, "Line", "Line.Region");
		
		addSingularRelation(ACLineSegment.class, EquipmentContainer.class, "ACLineSegment", "Equipment.MemberOf_EquipmentContainer", 
				"setMemberOfEquipmentContainer");
		
		System.out.println("Import Complete!");
		
		//printIdentifiedMap();
		
		if(true){return;}
		
		
		
		for (String d : typeMap.keySet()){
			EscaType escaType = typeMap.get(d);
			String dataType = escaType.getDataType();
			NodeBreakerDataType entity = null;			
			
			if (Esca60Vocab.ANALOG_OBJECT.getLocalName().equals(dataType)){
				
			}
			else if (Esca60Vocab.TERMINAL_OBJECT.getLocalName().equals(dataType)){
				entity = createTerminal(escaType);						
			}
			else if (Esca60Vocab.SUBSTATION_OBJECT.getLocalName().equals(dataType)){
				entity = createSubstation(escaType);
			}
			else if (Esca60Vocab.VOLTAGELEVEL_OBJECT.getLocalName().equals(dataType)){
				entity = createVoltageLevel(escaType);
			}
			
			if (entity != null){
				nodeBreakerDao.persist(entity);
				identifiedMap.put(((IdentifiedObject)entity).getMrid(), entity);
			}
						
//			if(Esca60Vocab.ANALOG_OBJECT.getLocalName().equals(dataType)){
//				storeAnalog(nodeBreakerDao, typeMap.get(d));
//			}
//			else if (Esca60Vocab.ANALOGLIMITSET_OBJECT.getLocalName().equals(dataType)){
//				storeAnalogLimitSet(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.ANALOGLIMIT_OBJECT.getLocalName().equals(dataType)){
//				storeAnalogLimit(nodeBreakerDao, typeMap.get(d));
//			}
//			else if (Esca60Vocab.BREAKER_OBJECT.getLocalName().equals(dataType)){
//				storeBreaker(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.CONFORMLOAD_OBJECT.getLocalName().equals(dataType)){
//				storeConformLoad(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.CONNECTIVITYNODE_OBJECT.getLocalName().equals(dataType)){
//				storeConnectivityNode(nodeBreakerDao, typeMap.get(d));
//			}
//			else if("Disconnector".equals(dataType)){
//				storeDisconnector(nodeBreakerDao, typeMap.get(d));
//			}
//			else if("Discrete".equals(dataType)){
//				storeDiscrete(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.LINE_OBJECT.getLocalName().equals(dataType)){
//				storeLine(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.SUBSTATION_OBJECT.getLocalName().equals(dataType)){
//				storeSubstation(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.TERMINAL_OBJECT.getLocalName().equals(dataType)){
//				storeTerminal(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.TRANSFORMERWINDING_OBJECT.getLocalName().equals(dataType)){
//				storeTransformerWinding(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.VOLTAGELEVEL_OBJECT.getLocalName().equals(dataType)){
//				storeVoltageLevel(nodeBreakerDao, typeMap.get(d));
//			}
//			else if("BusbarSection".equals(dataType)){
//				storeBusBarSection(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.POWERTRANSFORMER_OBJECT.getLocalName().equals(dataType)){
//				storePowerTransformer(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.CURVEDATA_OBJECT.getLocalName().equals(dataType)){
//				storeCurveData(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.REGULARTIMEPOINT_OBJECT.getLocalName().equals(dataType)) {
//				storeRegularTimePoint(nodeBreakerDao, typeMap.get(d));
//			}
//			else if(Esca60Vocab.TAPCHANGER_OBJECT.getLocalName().equals(dataType)){
//				storeTapChanger(nodeBreakerDao, typeMap.get(d));
//			}
//			else{
//				System.out.println("Datatype: "+dataType);
//			}
			
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
