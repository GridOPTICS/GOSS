package pnnl.goss.server.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.DataSourceType;

@Component(service = DataSourceRegistry.class)
public class DataSourceRegistryImpl implements DataSourceRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRegistryImpl.class);

    private final Map<String, DataSourceObject> dataSourceMap = new ConcurrentHashMap<>();
    private final Map<ServiceReference<DataSourceObject>, DataSourceObject> serviceRefMap = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "datasourceRemoved")
    public void datasourceAdded(ServiceReference<DataSourceObject> ref, DataSourceObject obj) {
        log.debug("Datasource registered: " + obj.getName());
        dataSourceMap.put(obj.getName(), obj);
        serviceRefMap.put(ref, obj);
    }

    public void datasourceRemoved(ServiceReference<DataSourceObject> ref) {
        log.debug("Removing datasource: " + serviceRefMap.get(ref).getName());
        DataSourceObject toRemove = serviceRefMap.remove(ref);
        dataSourceMap.remove(toRemove);
    }

    @Override
    public DataSourceObject get(String key) {
        DataSourceObject obj = dataSourceMap.get(key);

        return obj;
    }

    @Override
    public Map<String, DataSourceType> getAvailable() {
        Map<String, DataSourceType> map = new HashMap<>();

        for (DataSourceObject o : dataSourceMap.values()) {
            map.put(o.getName(), o.getDataSourceType());
        }

        return map;
    }

    @Override
    public void add(String key, DataSourceObject obj) {
        dataSourceMap.put(key, obj);
    }

    @Override
    public void remove(String key) {
        dataSourceMap.remove(key);
    }

}
