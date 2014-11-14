package pnnl.goss.core.client;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Session session;
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
		wasStarted = true;
	}
	
	public String getStatus(){
		if (wasStarted) 
			return STATUS_BROKER_STARTED;
		
		return STATUS_BROKER_STOPPED;
	}
	
	public void stop() throws Exception{
		if (!wasStarted) throw new Exception("Broker was not started");
		
		broker.stop();
		broker.waitUntilStopped();
		wasStarted = false;
	}
	
}
