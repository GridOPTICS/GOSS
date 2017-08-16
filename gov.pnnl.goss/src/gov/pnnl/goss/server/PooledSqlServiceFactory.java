package gov.pnnl.goss.server;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Inject;
import org.apache.felix.dm.annotation.api.Property;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import gov.pnnl.goss.server.api.DataSourceBuilder;
import gov.pnnl.goss.server.api.DataSourceObject;

@Component(
	properties=@Property(
			name=Constants.SERVICE_PID,
			value="pnnl.goss.sql.datasource")
)
public class PooledSqlServiceFactory implements ManagedServiceFactory{

	@Inject
	private volatile DependencyManager dm;

	// Map of service pid to the actual component.  Note we use long form
	// of component because it is different than the annotation component
	// used on the top of the class.
	private final Map<String, org.apache.felix.dm.Component> components = new ConcurrentHashMap<>();

	@Override
	public String getName() {
		return "Pooled Sql Service Factory";
	}

	private boolean isRequiredKey(String k){
		switch (k){
		case DataSourceBuilder.DATASOURCE_USER:
		case DataSourceBuilder.DATASOURCE_PASSWORD:
		case DataSourceBuilder.DATASOURCE_URL:
		case "name":
			return true;

		default:
			return false;
		}
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		Map<String, String> props = new HashMap<>();
		Map<String, String> otherProps = new HashMap<>();

		Enumeration<String> keys = properties.keys();

		while(keys.hasMoreElements()){
			String key= keys.nextElement();

			String value = (String)properties.get(key);

			if (isRequiredKey(key)){
				if (value == null || value.isEmpty()){
					throw new ConfigurationException(key, "Must be specified!");
				}
				props.put(key, value);
			}
			else{
				if (value != null && value.isEmpty()){
					otherProps.put(key, value);
				}
			}
		}

		String datasourceDriver = "com.mysql.jdbc.Driver";
		if (otherProps.containsKey(DataSourceBuilder.DATASOURCE_DRIVER)){
			datasourceDriver = otherProps.get(DataSourceBuilder.DATASOURCE_DRIVER);
			otherProps.remove(DataSourceBuilder.DATASOURCE_DRIVER);
		}

		PooledSqlServiceImpl service = new PooledSqlServiceImpl(
				props.get("name"),
				props.get(DataSourceBuilder.DATASOURCE_URL),
				props.get(DataSourceBuilder.DATASOURCE_USER),
				props.get(DataSourceBuilder.DATASOURCE_PASSWORD),
				datasourceDriver, otherProps);

		org.apache.felix.dm.Component c = dm.createComponent()
				.setInterface(DataSourceObject.class.getName(), null).setImplementation(service);

		components.put(pid, c);
		dm.add(c);
	}

	@Override
	public void deleted(String pid) {
		dm.remove(components.remove(pid));
	}


}
