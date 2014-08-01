package pnnl.goss.server.core.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.server.core.GossDataServices;

@Provides
@Instantiate
@Component(immediate=true)
public class GossDataServicesImpl implements GossDataServices {
	
	private static final Logger log = LoggerFactory.getLogger(GossDataServicesImpl.class);
	private Hashtable<String, Object> dataservices;

	public GossDataServicesImpl(){
		log.debug("Constructing");
		dataservices = new Hashtable<String, Object>();
	}
	
	@Override
	public void registerData(String serviceName, Object dataservice) {
		log.debug("Registering: " + serviceName);
		dataservices.put(serviceName, dataservice);
	}

	@Override
	public void unRegisterData(String serviceName) {
		log.debug("Unregistering: "+serviceName);
		dataservices.remove(serviceName);		
	}

	@Override
	public Connection getPooledConnection(String serviceName) {
		log.debug("Getting ppoled connection: "+serviceName);
		Object value = dataservices.get(serviceName);
		Connection conn = null;
		try {			
			if(value != null){
				if(value instanceof DataSource){
					conn = ((DataSource)value).getConnection();
					log.debug("connection retrieved");
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return conn;
	}

	@Override
	public Object getDataService(String serviceName) {
		log.debug("Retrieving service: "+serviceName);
		return dataservices.get(serviceName);
	}
	
	@Invalidate
	public void releaseServices(){
		log.debug("Clearing services");
		dataservices.clear();
	}

	@Override
	public boolean contains(String serviceName) {
		return dataservices.contains(serviceName);
	}
}
