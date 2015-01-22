package pnnl.goss.core.server.internal;

import static pnnl.goss.core.GossCoreContants.PROP_DATASOURCES_CONFIG;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.BasicDataSourceCreator;
import pnnl.goss.core.server.GossDataServices;

// @Component(immediate=true, managedservice = PROP_DATASOURCES_CONFIG)
public class GossDataServicesImpl implements GossDataServices {

    private static final Logger log = LoggerFactory.getLogger(GossDataServicesImpl.class);

    private BasicDataSourceCreator datasourceCreator;

    /**
     * Holds services that have been registered with the system.
     */
    private Hashtable<String, Object> dataservices;
    /**
     * Configuration object that is passed to the object
     */
    private Hashtable<String, String> properties = new Hashtable<String, String>();

    private GossDataServicesImpl(){
        dataservices = new Hashtable<>();
    }

    public GossDataServicesImpl(BasicDataSourceCreator datasourceCreator){
        this();
        this.datasourceCreator = datasourceCreator;
    }
    public GossDataServicesImpl(BasicDataSourceCreator datasourceCreator,
            Dictionary<String, Object> config){
        this();
        this.datasourceCreator = datasourceCreator;
        update(config);
    }

    public void update(@SuppressWarnings("rawtypes") Dictionary config){
        properties.clear();
        @SuppressWarnings("rawtypes")
        Enumeration nummer = config.keys();
        Hashtable<String, ConstructableDatasource> possibleConstruction =
                new Hashtable<>();

        while(nummer.hasMoreElements()){
            String key = (String)nummer.nextElement();
            String value = (String)config.get(key);
            log.debug("Adding property key: " + key);
            switch(value.toUpperCase()){
            case "MYSQL":
                ConstructableDatasource ds = new ConstructableDatasource();
                possibleConstruction.put(key, ds);
            }

            properties.put(key,  (String)config.get(key));
        }

        for(Entry<String, ConstructableDatasource> entry: possibleConstruction.entrySet()){
            ConstructableDatasource ds = entry.getValue();
            ds.username = properties.get(entry.getKey()+".user");
            ds.password = properties.get(entry.getKey()+".password");
            ds.uri = properties.get(entry.getKey()+".uri");
            ds.driver = properties.get(entry.getKey()+".driver");

            try {
                BasicDataSource actualDs = datasourceCreator.create(ds.uri,
                        ds.username, ds.password, ds.driver);
                dataservices.put(entry.getKey(), actualDs);
            } catch (Exception e) {
                log.error("Unable to create database connection for: "+
                            entry.getKey());
            }
        }

    }

    private class ConstructableDatasource{
        public String username;
        public String password;
        public String uri;
        public String driver;
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

    public void releaseServices(){
        log.debug("Clearing services");
        dataservices.clear();
    }

    @Override
    public boolean contains(String serviceName) {
        return dataservices.contains(serviceName);
    }

    @Override
    public Collection<String> getAvailableDataServices() {
        return Collections.unmodifiableCollection(dataservices.keySet());
    }

    @Override
    public Collection<String> getPropertyKeys() {
        return Collections.unmodifiableCollection(properties.keySet());
    }

    @Override
    public String getPropertyValue(String key) {
        return properties.get(key);
    }
}
