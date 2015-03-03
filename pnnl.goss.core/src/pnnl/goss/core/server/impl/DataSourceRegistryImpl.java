package pnnl.goss.core.server.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourceRegistry;

@Component
public class DataSourceRegistryImpl implements DataSourceRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceRegistryImpl.class);
	
	private final Map<String, DataSourceObject> dataSourceMap = new ConcurrentHashMap<>();
	private final Map<ServiceReference<DataSourceObject>, DataSourceObject> serviceRefMap = new ConcurrentHashMap<>();

	@ServiceDependency(removed="datasourceRemoved", required=false)
	public void datasourceAdded(ServiceReference<DataSourceObject> ref, DataSourceObject obj){
		log.debug("Datasource registered: " + obj.getName());
		dataSourceMap.put(obj.getName(), obj);
		serviceRefMap.put(ref, obj);
	}
	
	public void datasourceRemoved(ServiceReference<AuthorizationHandler> ref){
		log.debug("Removing datasource: " + serviceRefMap.get(ref).getName());
		DataSourceObject toRemove = serviceRefMap.remove(ref);
		dataSourceMap.remove(toRemove);
		toRemove.onRemoved();
	}

	@Override
	public DataSourceObject get(String key) {
		DataSourceObject obj = dataSourceMap.get(key);
		if(obj != null){
			obj.onGet();
		}
		return obj;
	}
}
