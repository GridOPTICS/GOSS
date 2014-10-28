package pnnl.goss.core.client;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.TransportConnector;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractActivemqBrokerTest {
	
	protected BrokerService broker;
	protected Connection connection;
    protected TransportConnector connector;
//    protected int consumersBeforeDispatchStarts;
//    protected int timeBeforeDispatchStarts;
    
	@Before
	public void setup() throws Exception{
		broker = new BrokerService();
		broker.setPersistent(false);
		broker.setUseJmx(false);
		connector = broker.addConnector("tcp://0.0.0.0:59595");
		broker.start();
	}
	
	@After
	public void teardown() throws Exception{
		connection.stop();
		connector.stop();
		broker.stop();
	}

}
