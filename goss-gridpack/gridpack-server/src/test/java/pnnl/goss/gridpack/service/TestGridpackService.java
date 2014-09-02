package pnnl.goss.gridpack.service;

import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.gridpack.common.datamodel.GridpackBus;
import pnnl.goss.gridpack.common.datamodel.GridpackPowergrid;
import pnnl.goss.gridpack.service.GridpackServiceImpl;
import pnnl.goss.powergrid.server.PowergridServerActivator;
import pnnl.goss.server.core.BasicDataSourceCreator;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.internal.GossDataServicesImpl;
import pnnl.goss.util.Utilities;

public class TestGridpackService {
	private static Logger log = LoggerFactory.getLogger(TestGridpackService.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void replacePropertiesFromHome(Dictionary toReplace, String propertiesFile){
		File gossProperties = new File(System.getProperty("user.home")+"\\.goss\\"+propertiesFile);
		
		if(!gossProperties.exists()){
			log.error("Properties File Doesn't exist!\n\t"+gossProperties.toString());
			return;
		}
		
		Dictionary privateProperties = Utilities.loadProperties(gossProperties.getAbsolutePath(), false);
		
		Enumeration propEnum = toReplace.keys();
		
		while(propEnum.hasMoreElements()){
			String k = (String) propEnum.nextElement();
			String v = (String) toReplace.get(k);
			
			if (v != null && v.startsWith("${") && v.endsWith("}")){
				String keyInPrivate = v.substring(2, v.length() - 1);
				if (privateProperties.get(keyInPrivate) != null){
					toReplace.put(k, privateProperties.get(keyInPrivate));
				}
			}
		}		
	}

	public static void main(String[] args) throws Exception {
		

//		Dictionary dataSourcesConfig = Utilities.loadProperties(PROP_DATASOURCES_CONFIG);
//		// Replaces the ${..} with values from the goss.properties file.
//		replacePropertiesFromHome(dataSourcesConfig, "goss.properties");
//
//		
//		BasicDataSourceCreator bdc = new BasicDataSourceCreator();
//		String uri = (String) dataSourcesConfig.get(PowergridServerActivator.PROP_POWERGRID_URI);
//		String user = (String) dataSourcesConfig.get(PowergridServerActivator.PROP_POWERGRID_USER);
//		String pass = (String) dataSourcesConfig.get(PowergridServerActivator.PROP_POWERGRID_PASSWORD);
//					
//		BasicDataSource datasource = bdc.create(uri, user, pass);
//				
//		GossDataServices dataServices = new GossDataServicesImpl();
//		
//		dataServices.registerData(PowergridServerActivator.PROP_POWERGRID_DATASERVICE, datasource);
//		
//		GridpackServiceImpl service = new GridpackServiceImpl(dataServices);
//		
//		//GridpackBuses buses = service.getBuses("Greek-118");
//		//Collection<GridpackBus> buses = service.getBuses("Greek-118");
//		Object pg = service.getGridpackGrid("Greek-118");
//		
//		if (pg instanceof GridpackPowergrid)
//		{
//			Collection<GridpackBus> buses = ((GridpackPowergrid)pg).getBuses(); 
//			
//			for (GridpackBus bus:buses){
//				System.out.println(bus.getBusName());
//				if (bus.getLoads() != null && bus.getLoads().size() > 0){
//					System.out.println(String.format("Bus %d has %d Loads", bus.getBusNumber(), bus.getLoads().size()));
//				}
//				
//				if (bus.getShunts() != null && bus.getShunts().size() > 0){
//					System.out.println(String.format("Bus %d has %d Shunts", bus.getBusNumber(), bus.getShunts().size()));
//				}
//				
//				if (bus.getGenerators() != null && bus.getGenerators().size() > 0){
//					System.out.println(String.format("Bus %d has %d Generators", bus.getBusNumber(), bus.getGenerators().size()));
//				}
//			}
//		}
	}

}
