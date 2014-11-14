package pnnl.goss.core.client;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class allows us to wrap up a broker so that we can easily start
 * and stop it in order to test functionality.
 * 
 * @author Craig Allwardt
 *
 */
public class ActivemqTestBroker
{
	private static final Logger log = LoggerFactory.getLogger(ActivemqTestBroker.class);
	
	public final String NORMAL_BROKER_URI = "tcp://localhost:61616";
	public final String STATUS_BROKER_STARTED = "STARTED";
	public final String STATUS_BROKER_STOPPED = "STOPPED";
	
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Set<Session> sessions = new LinkedHashSet<>();
	private Destination destination;
	private BrokerService broker;
	private boolean wasStarted = false;
	
	public void startNormal() throws Exception{
		
		if(wasStarted) throw new Exception("Already started broker");
		
		broker = new BrokerService();
		broker.addConnector(NORMAL_BROKER_URI);
		broker.setPersistent(false);
		broker.start();
		broker.waitUntilStarted();		
		connectionFactory = new ActiveMQConnectionFactory(NORMAL_BROKER_URI);
		connection = connectionFactory.createConnection();
		wasStarted = true;
	}
	
	public Session createSession() throws Exception{
		
		if (!wasStarted) throw new Exception("Must call a start method before creating session");
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		sessions.add(session);
		return session;
	}
	
	public Session createQueue(String destination) throws Exception {
		
		Session session = createSession();
		session.createQueue(destination);
		return session;
	}
	
	public String getStatus(){
		if (wasStarted) 
			return STATUS_BROKER_STARTED;
		
		return STATUS_BROKER_STOPPED;
	}
	
	public void stop() throws Exception{
		if (!wasStarted) throw new Exception("Broker was not started");
		for (Session s: sessions){
			try{
				s.close();
			}catch(Exception ex){
				// pass
			}
		}
		sessions.clear();
		connectionFactory = null;
		broker.stop();
		broker.waitUntilStopped();
		wasStarted = false;
	}
	
}
