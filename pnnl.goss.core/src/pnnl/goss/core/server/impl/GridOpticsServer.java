/*
    Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE

    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.core.server.impl;

import static pnnl.goss.core.GossCoreContants.PROP_ACTIVEMQ_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslBrokerService;
import org.apache.activemq.shiro.ShiroPlugin;
import org.apache.activemq.shiro.env.IniEnvironment;
import org.apache.activemq.shiro.subject.ConnectionSubjectFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.mgt.SecurityManager;
import org.iq80.leveldb.util.FileUtils;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.PermissionAdapter;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.ServerControl;


@Component
public class GridOpticsServer implements ServerControl {

    private static final Logger log = LoggerFactory.getLogger(GridOpticsServer.class);
    private static final String CONFIG_PID = "pnnl.goss.core.server";
    
    private static final String PROP_USE_AUTH = "goss.use.authorization";
    private static final String PROP_START_BROKER = "goss.start.broker";
    private static final String PROP_CONNECTION_URI = "goss.broker.uri";
    private static final String PROP_OPENWIRE_TRANSPORT = "goss.openwire.uri";
    private static final String PROP_STOMP_TRANSPORT = "goss.stomp.uri";
    private static final String PROP_SSL_TRANSPORT = "goss.ssl.uri";
    
    private static final String PROP_SSL_ENABLED = "ssl.enabled";
    private static final String PROP_SSL_CLIENT_KEYSTORE = "client.keystore";
    private static final String PROP_SSL_CLIENT_KEYSTORE_PASSWORD = "client.keystore.password";
    private static final String PROP_SSL_CLIENT_TRUSTSTORE = "client.truststore";
    private static final String PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD = "client.truststore.password";
    
    private static final String PROP_SSL_SERVER_KEYSTORE = "server.keystore";
    private static final String PROP_SSL_SERVER_KEYSTORE_PASSWORD = "server.keystore.password";
    private static final String PROP_SSL_SERVER_TRUSTSTORE = "server.truststore";
    private static final String PROP_SSL_SERVER_TRUSTSTORE_PASSWORD = "server.truststore.password";
    
    private static final String PROP_SYSTEM_MANAGER = "goss.system.manager";
    private static final String PROP_SYSTEM_MANAGER_PASSWORD = "goss.system.manager.password";
            
    private BrokerService broker;
    private Connection connection;
    private Session session;
    private Destination destination;
    
    // System manager username/password (required * privleges on the message bus)
    private String systemManager = null;
    private String systemManagerPassword = null;
    
    // Should we automatically start the broker?
    private boolean shouldStartBroker = false;
    // The connectionUri to create if shouldStartBroker is set to true.
    private String connectionUri = null;
    // The tcp transport for openwire
    private String openwireTransport = null;
    // The ssl transport for connections to the server
    private String sslTransport = null;
    // The tcp transport for stomp
    private String stompTransport = null;
    // Topic to listen on for receiving requests
    private String requestQueue = null;
    
    // SSL Parameters
    private boolean sslEnabled = false;
    private String sslClientKeyStore = null;
    private String sslClientKeyStorePassword = null;
    private String sslClientTrustStore = null;
    private String sslClientTrustStorePassword = null;
    
    private String sslServerKeyStore = null;
    private String sslServerKeyStorePassword = null;
    private String sslServerTrustStore = null;
    private String sslServerTrustStorePassword = null;
    
    // A list of consumers all listening to the requestQueue
    private final List<ServerConsumer> consumers = new ArrayList<>(); 
     
    private ConnectionFactory connectionFactory = null;
    
    @ServiceDependency
    private volatile SecurityManager securityManager;
    
    
    @ServiceDependency
    private volatile RequestHandlerRegistry handlerRegistry;
    
    @ServiceDependency
    private volatile GossRealm permissionAdapter;
    
    /**
     * Return a default value if the passed string is null or empty,
     * or if the value starts with a ${ (assumes that a property
     * wasn't set in a properties file.).
     * 
     * @param value			The value to interrogate.
     * @param defaultValue  A default value to return if our checks weren't valid
     * @return				The value or defaultValue
     */
    private String getProperty(String value, String defaultValue){
    	String retValue = defaultValue;
    	
    	if (value != null && !value.isEmpty()){
    		// Let the value pass through because it doesn't
    		// start with ${
    		if (!value.startsWith("${")){
    			retValue = value;
    		}
    	}
    	
    	return retValue;
    }
        
        
    @ConfigurationDependency(pid=CONFIG_PID)
    public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
    	
    	if (properties != null) {
    		
    		systemManager = getProperty((String) properties.get(PROP_SYSTEM_MANAGER),
    				"system");
    		systemManagerPassword = getProperty((String) properties.get(PROP_SYSTEM_MANAGER_PASSWORD),
    				"manager"); 
    		    	
    		shouldStartBroker = Boolean.parseBoolean(
    				getProperty((String) properties.get(PROP_START_BROKER), "true"));
    		
    		connectionUri = getProperty((String)properties.get(PROP_CONNECTION_URI),
    				"tcp://localhost:61616");
    		
	    	openwireTransport = getProperty((String) properties.get(PROP_OPENWIRE_TRANSPORT),
	    			"tcp://localhost:61616");
	    	
	    	stompTransport = getProperty((String) properties.get(PROP_STOMP_TRANSPORT),
	    			"tcp://localhost:61613");
	    	
	    	requestQueue = getProperty((String) properties.get(GossCoreContants.PROP_REQUEST_QUEUE)
	    			,"Request");
	    	
	    	// SSL IS DISABLED BY DEFAULT.
	    	sslEnabled = Boolean.parseBoolean(
	    			getProperty((String) properties.get(PROP_SSL_ENABLED)
	    			,"false"));
	    	
	    	sslTransport = getProperty((String) properties.get(PROP_SSL_TRANSPORT)
	    			,"tcp://localhost:61443");
	    	
	    	sslClientKeyStore = getProperty((String) properties.get(PROP_SSL_CLIENT_KEYSTORE)
	    			,null);
	    	sslClientKeyStorePassword = getProperty((String) properties.get(PROP_SSL_CLIENT_KEYSTORE_PASSWORD)
	    			,null);
	    	sslClientTrustStore = getProperty((String) properties.get(PROP_SSL_CLIENT_TRUSTSTORE)
	    			,null);
	    	sslClientTrustStorePassword = getProperty((String) properties.get(PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD)
	    			,null);
	    	sslServerKeyStore = getProperty((String) properties.get(PROP_SSL_SERVER_KEYSTORE)
	    			,null);
	    	sslServerKeyStorePassword = getProperty((String) properties.get(PROP_SSL_SERVER_KEYSTORE_PASSWORD)
	    			,null);
	    	sslServerTrustStore = getProperty((String) properties.get(PROP_SSL_SERVER_TRUSTSTORE)
	    			,null);
	    	sslServerTrustStorePassword = getProperty((String) properties.get(PROP_SSL_SERVER_TRUSTSTORE_PASSWORD)
	    			,null);
	    	
	    	
    	}
        
    }
    
    public Session getSession(){
		return session;
	}
    
    /**
     * Consults the variables created in the update method for whether
     * there is enough information to create ssl broker and that the
     * ssl.enable property is set to true.
     * 
     * @return true if the server supports ssl and ssl.enabled is true.
     */
    private boolean shouldUsSsl(){
    	// Do we want ssl from the config file?
    	boolean useSsl = sslEnabled;
    	
    	if (useSsl) {
    		
    		// FileNameUtils.getName will return an empty string if the file
    		// does not exist.
    		if (FilenameUtils.getName(sslClientKeyStore).isEmpty() ||
    				FilenameUtils.getName(sslClientTrustStore).isEmpty())
    		{
    			useSsl = false;
    		}
    	}
    	
    	return useSsl;
    	
    }
    
    /**
     * Creates a broker with shiro security plugin installed.
     * 
     * After this function the broker variable 
     */
    private void createBroker() throws Exception{
    	// Create shiro broker plugin
		ShiroPlugin shiroPlugin = new ShiroPlugin();
				
		shiroPlugin.setSecurityManager(securityManager);
		//shiroPlugin.setIniConfig("conf/shiro.ini");
		
		//shiroPlugin.setIni(new IniEnvironment("conf/shiro.ini"));
		//shiroPlugin.getSubjectFilter().setConnectionSubjectFactory(subjectConnectionFactory);
		    		
		// Configure how we are going to use it.
		//shiroPlugin.setIniConfig(iniConfig);
		
		try {
			if (shouldUsSsl()){
				broker = new SslBrokerService();
				broker.setPersistent(false);
				
				KeyManager[] km = getKeyManager(sslServerKeyStore, sslServerKeyStorePassword);
		        TrustManager[] tm = getTrustManager(sslClientTrustStore);
		        ((SslBrokerService) broker).addSslConnector(sslTransport, km, tm, null);
		        log.debug("Starting broker with ssl connector: " + sslTransport);

			} else {
				broker = new BrokerService();
				broker.addConnector(openwireTransport);
			}
			//broker.addConnector(stompTransport);
			broker.setPlugins(new BrokerPlugin[]{shiroPlugin});
			
    		broker.start();
		} catch (Exception e) {
			log.debug("Error Starting Broker", e);
			//System.err.println(e.getMessage());;
		}
    }
        
    @Override
    @Start
	public void start() {
    	
		// If goss should have start the broker service then this will be set.
    	// this variable is mapped from goss.start.broker
    	if (shouldStartBroker) {
    		try {
				createBroker();
			} catch (Exception e) {
				e.printStackTrace();
				broker = null;
				log.error("Error starting broker: ", e);
				throw SystemException.wrap(e);
			}
    	}
    	
    	try {
    		if (shouldUsSsl()){
    			connectionFactory = new ActiveMQSslConnectionFactory(sslTransport);
    			
    			((ActiveMQSslConnectionFactory) connectionFactory).setTrustStore(sslClientTrustStore); //sslClientTrustStore);
    			((ActiveMQSslConnectionFactory) connectionFactory).setTrustStorePassword(sslClientTrustStorePassword); //sslClientTrustStorePassword);
  	        
    		}
    		else{
    			connectionFactory = new ActiveMQConnectionFactory(openwireTransport);
    		}
    		
    		connection = connectionFactory.createConnection("system", "manager");
    		connection.start();			
		} catch (Exception e) {
			log.debug("Error Connecting to ActiveMQ", e);
			if (shouldStartBroker){
				try {
					if (broker != null){
						broker.stop();
						broker.waitUntilStopped();	
					}					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
			throw SystemException.wrap(e, ConnectionCode.CONNECTION_ERROR);
		}
    	
    	
    	try {
	    	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    	destination = session.createQueue(requestQueue);
	    	
	    	for(int i=0; i<1; i++){
	    		System.out.println("Creating consumer: "+i);
	    		consumers.add(new ServerConsumer()
	    				.setDestination(destination)
	    				.setSession(session)
	    				.setRegistryHandler(handlerRegistry)
	    				.consume());
	    	}
    	} catch (JMSException e) {
			throw SystemException.wrap(e, ConnectionCode.CONSUMER_ERROR);
		}
	}
    
    private void createAuthenticatedConnectionFactory(String username, String password) throws JMSException {
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionUri);
		
		// Todo find out how we get password from user via config file?
		
		factory.setUserName(username);
		factory.setPassword(password);
		connectionFactory = factory;
		
	}


	@Override
	@Stop
	public void stop() throws SystemException {
		
		try {
			consumers.clear();
			
			if(session != null) {
				session.close();
			}
			if (connection != null){
				connection.close();
			}
			if (shouldStartBroker){
				if(broker != null) {
					broker.stop();
					broker.waitUntilStopped();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SystemException.wrap(e, ConnectionCode.CLOSING_ERROR);
		}		
		finally{
			session= null;
			connection = null;
			destination = null;
			broker = null;
			connectionFactory = null;
		}
	}



	@Override
	public boolean isRunning() {
		if (broker == null) return false;
		
		return broker.isStarted();
	}
	
	public static TrustManager[] getTrustManager(String clientTrustStore) throws Exception {
        TrustManager[] trustStoreManagers = null;
        KeyStore trustedCertStore = KeyStore.getInstance("jks"); //ActiveMQSslConnectionFactoryTest.KEYSTORE_TYPE);
        
        trustedCertStore.load(new FileInputStream(clientTrustStore), null);
        TrustManagerFactory tmf  = 
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
  
        tmf.init(trustedCertStore);
        trustStoreManagers = tmf.getTrustManagers();
        return trustStoreManagers; 
    }

    public static KeyManager[] getKeyManager(String serverKeyStore, String serverKeyStorePassword) throws Exception {
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());  
        KeyStore ks = KeyStore.getInstance("jks"); //ActiveMQSslConnectionFactoryTest.KEYSTORE_TYPE);
        KeyManager[] keystoreManagers = null;
        
        byte[] sslCert = loadClientCredential(serverKeyStore);
        
       
        if (sslCert != null && sslCert.length > 0) {
            ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
            ks.load(bin, serverKeyStorePassword.toCharArray());
            kmf.init(ks, serverKeyStorePassword.toCharArray());
            keystoreManagers = kmf.getKeyManagers();
        }
        return keystoreManagers;          
    }

    private static byte[] loadClientCredential(String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }
        FileInputStream in = new FileInputStream(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int i = in.read(buf);
        while (i  > 0) {
            out.write(buf, 0, i);
            i = in.read(buf);
        }
        in.close();
        return out.toByteArray();
    }
    
    
    

//    public GridOpticsServer(GossRequestHandlerRegistrationService handlerService,
//        Dictionary<String, Object> coreConfiguration, boolean startBroker) throws Exception{
//
//        Dictionary<String, Object> config = coreConfiguration;
//        String brokerURI = (String)config.get(PROP_OPENWIRE_URI);
//        URI uri = URI.create(brokerURI);
//        String user = (String)config.get(GossCoreContants.PROP_SYSTEM_USER);
//        String pw = (String)config.get(GossCoreContants.PROP_SYSTEM_PASSWORD);
//
//        log.debug("Creating gridoptics server\n\tbrokerURI:"+
//                brokerURI+"\n\tsystem user: "+user);
//
//
//
//        //Needed for standalone server instance
//        if(startBroker){
//            startBroker(config);
//        }
//
//        makeActiveMqConnection(uri, user, pw);
//
//        consumer = new ServerConsumer(coreConfiguration, handlerService);
//    }
//
//
//    private void startBroker(Dictionary<String, Object> config) throws Exception {
//
//        if (config.get(PROP_ACTIVEMQ_CONFIG) != null){
//            String brokerConfig = "xbean:" + (String) config.get(PROP_ACTIVEMQ_CONFIG);
//            log.debug("Starting broker using config: " + brokerConfig);
//
//            System.setProperty("activemq.base", System.getProperty("user.dir"));
//            log.debug("ActiveMQ base directory set as: "+System.getProperty("activemq.base"));
//            broker = BrokerFactory.createBroker(brokerConfig, true);
//            broker.setDataDirectory(System.getProperty("activemq.base")+"/data");
//        }
//        else{
//            log.debug("Broker started not using xbean "+ (String)config.get(PROP_OPENWIRE_URI));
//            broker = new BrokerService();
//            broker.addConnector((String)config.get(PROP_OPENWIRE_URI));
//            log.warn("Persistent storage is off");
//            String datadir = System.getProperty("java.io.tmpdir") + File.separatorChar
//                    + "gossdata";
//            broker.setDataDirectory(datadir);
//            broker.start();
//        }
//        broker.waitUntilStarted();
//
//    }
//
//    private void makeActiveMqConnection(URI brokerUri, String systemUser, String systemPW) throws JMSException{
//        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUri);
//        factory.setUseAsyncSend(true);
//        //Use system login account
//        if(systemUser!=null){
//            factory.setUserName(systemUser);
//        }
//        if(systemPW!=null){
//            factory.setPassword(systemPW);
//        }
//
//        log.debug("Creating connection to: "+brokerUri +" using account: "+ systemUser);
//        connection = (ActiveMQConnection)factory.createConnection();
//        connection.start();
//    }

//    public static Connection getConnection() throws NullPointerException{
//        if(connection==null)
//            throw new NullPointerException("Cannot connect to server. Create GridOPTICSServer instance first.");
//
//        return connection;
//    }
//
//    public void close() throws JMSException {
//    	
//        if (connection != null) {
//            connection.close();
//        }
//        if (consumer != null){
//            consumer = null;
//        }
//        if (broker != null){
//            try {
//                broker.stop();
//                broker.waitUntilStopped();
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        log.debug("Closing connection");
//        broker = null;
//        connection = null;
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        //Make really sure that the connection gets closed
//        //close();
//        super.finalize();
//    }



	

}
