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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.ServerControl;


@Component
public class GridOpticsServer implements ServerControl {

    private static final Logger log = LoggerFactory.getLogger(GridOpticsServer.class);
    private static final String CONFIG_PID = "pnnl.goss.core.server";
    
    private static final String PROP_USE_AUTH = "goss.use.authorization";
    private static final String PROP_START_BROKER = "goss.start.broker";
    private static final String PROP_CONNECTIOn_URI = "goss.broker.uri";
    private static final String PROP_OPENWIRE_TRANSPORT = "goss.openwire.uri";
    private static final String PROP_STOMP_TRANSPORT = "goss.stomp.uri";
    
    
    
    private BrokerService broker;
    private Connection connection;
    private Session session;
    private Destination destination;
    
    // Should we automatically start the broker?
    private boolean shouldStartBroker = false;
    // The connectionUri to create if shouldStartBroker is set to true.
    private String connectionUri = null;
    // The tcp transport for openwire
    private String openwireTransport = null;
    // The tcp transport for stomp
    private String stompTransport = null;
    // Topic to listen on for receiving requests
    private String requestQueue = null;
    
    // A list of consumers all listening to the requestQueue
    private final List<ServerConsumer> consumers = new ArrayList<>(); 
        
    
    private ConnectionFactory connectionFactory = null;
    
    
    @ServiceDependency
    private volatile RequestHandlerRegistry handlerRegistry;
        
    @ConfigurationDependency(pid=CONFIG_PID)
    public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
    	
    	if (properties != null) {
    		    	
    		shouldStartBroker = Boolean.parseBoolean(Optional
    				.ofNullable((String) properties.get(PROP_START_BROKER))
    				.orElse("true"));
    		
    		connectionUri = Optional
    				.ofNullable((String)properties.get(PROP_CONNECTIOn_URI))
    				.orElse("tcp://localhost:61616");
    		
	    	openwireTransport = Optional
	    			.ofNullable((String) properties.get(PROP_OPENWIRE_TRANSPORT))
	    			.orElse("tcp://localhost:61616");
	    	
	    	stompTransport = Optional
	    			.ofNullable((String) properties.get(PROP_STOMP_TRANSPORT))
	    			.orElse("tcp://localhost:61613");
	    	
	    	requestQueue = Optional
	    			.ofNullable((String) properties.get(GossCoreContants.PROP_REQUEST_QUEUE))
	    			.orElse("Request");
	    	
	    	//start();
    	}
//    	else {
//    		if(isRunning()){
//    			stop();
//    		}
//    	}
    	
    	
//	    	
//	    	validatedAndStart();
//	    	
//	    	String brokerConfig = "xbean:conf/" + (String) properties.get("goss.broker.file"); // config.get(PROP_ACTIVEMQ_CONFIG);
//	        log.debug("Starting broker using config: " + brokerConfig);
//	        System.setProperty("activemq.base", System.getProperty("user.dir"));
//	        log.debug("ActiveMQ base directory set as: "+System.getProperty("activemq.base"));
//	        //log.debug("Broker started not using xbean "+ (String)config.get(PROP_OPENWIRE_URI));
//	        try {
//	        	openWireUri = (String)properties.get(PROP_OPENWIRE_URI);
//	        	start();
//	        			
////	        	broker = new BrokerService();
////	        
////				broker.addConnector((String)properties.get(PROP_OPENWIRE_URI));
////				
////		        log.warn("Persistent storage is off");
////		        String datadir = System.getProperty("java.io.tmpdir") + File.separatorChar
////		                + "gossdata";
////		        broker.setDataDirectory(datadir);
////		        // TODO allow configuration.
////		        // Should start less than 10 seconds.
////		        broker.start(); //.waitUntilStarted(10000);
//	        } catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//        
//        try {
//			broker = BrokerFactory.createBroker(brokerConfig, true);
//			broker.setDataDirectory(System.getProperty("activemq.base")+"/data");
//			broker.waitUntilStarted();
//			String brokerURI = (String)properties.get(PROP_OPENWIRE_URI);
//			URI uri = URI.create(brokerURI);
//			makeActiveMqConnection(uri, "goss", "goss");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        
    }
    
    public Session getSession(){
		return session;
	}
        
    @Override
    @Start
	public void start() throws SystemException {
    	
		
    	if (shouldStartBroker) {
    		broker = new BrokerService();
    		broker.setPersistent(false);
    		try {
				broker.addConnector(openwireTransport);
				broker.addConnector(stompTransport);
	    		broker.start();
			} catch (Exception e) {
				SystemException.wrap(e, ConnectionCode.BROKER_START_ERROR);
			}
    		
    	}
    	
    	try {
    		connectionFactory = new ActiveMQConnectionFactory(connectionUri);
    		connection = connectionFactory.createConnection();
    		connection.start();			
		} catch (JMSException e) {
			SystemException.wrap(e, ConnectionCode.CONNECTION_ERROR);
		}
    	
    	
    	try {
	    	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    	destination = session.createQueue(requestQueue);
	    	
	    	for(int i=0; i<10; i++){
	    		System.out.println("Creating consumer: "+i);
	    		consumers.add(new ServerConsumer()
	    				.setDestination(destination)
	    				.setSession(session)
	    				.setRegistryHandler(handlerRegistry)
	    				.consume());
	    	}
    	} catch (JMSException e) {
			SystemException.wrap(e, ConnectionCode.CONSUMER_ERROR);
		}
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