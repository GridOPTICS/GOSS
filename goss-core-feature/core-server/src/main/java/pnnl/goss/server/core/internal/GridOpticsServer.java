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
package pnnl.goss.server.core.internal;

import java.net.URI;
import java.util.Dictionary;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import pnnl.goss.security.util.GossSecurityConstants;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;
import pnnl.goss.util.Utilities;


public class GridOpticsServer {
	
	public static final String BROKER_URI_PROP = "brokerURI";
	
	private static Connection connection;

	
	@SuppressWarnings("rawtypes")
	public GridOpticsServer(GossRequestHandlerRegistrationService service, boolean startBroker){
		try {
			Dictionary config = service.getCoreServerConfig();
			String brokerURI = (String)config.get(BROKER_URI_PROP);
			URI uri = URI.create(brokerURI);
			String user = (String)config.get(GossSecurityConstants.PROP_SYSTEM_USER);
			String pw = (String)config.get(GossSecurityConstants.PROP_SYSTEM_PW);
			
			//Needed for standalone server instance
			if(startBroker){
				startBroker();
			}
			
			makeActiveMqConnection(uri, user, pw);
			 		
//			if (service == null){
//				new ServerConsumer();
//			}
//			else{
				new ServerConsumer(service);
//			}
		} catch (JMSException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	//TO MY KNOWLEDGE THESE CONSTRUCTORS AREN'T NEEDED ANYMORE
//	public GridOpticsServer(URI brokerUri, GossRequestHandlerRegistrationService service){
//	try {
//		makeActiveMqConnection(brokerUri);
//		 		
//		if (service == null){
//			new ServerConsumer();
//		}
//		else{
//			new ServerConsumer(service);
//		}
//	} catch (JMSException e) {
//		e.printStackTrace();
//	}
//}
	
//	public GridOpticsServer(URI brokerUri){
//		this(brokerUri, null);
//	}
//	
//	public GridOpticsServer(GossRequestHandlerRegistrationService service) {
//		try{
//			if(connection==null){
//				//Create and start embedded Broker
//				//TODO: try vm transport for embedded broker
//				Utilities.getInstance();
//				System.setProperty("activemq.base", System.getProperty("user.dir"));
//				System.out.println("ActiveMQ base directory set as: "+System.getProperty("activemq.base"));
//				BrokerService broker = BrokerFactory.createBroker(Utilities.getProperty("brokerConfig"));
//				Utilities.setbrokerURI(broker.getTransportConnectors().get(0).getConnectUri());
//				broker.start();
//				
//				makeActiveMqConnection(Utilities.getbrokerURI());
//				
//				//Setup Consumer
//				new ServerConsumer(service);
//			}
//		
//		}
//		catch(Exception e ){
//			e.printStackTrace();
//		}
//	}
	
	private void startBroker() throws Exception {
		Utilities.getInstance();
		System.setProperty("activemq.base", System.getProperty("user.dir"));
		System.out.println("ActiveMQ base directory set as: "+System.getProperty("activemq.base"));
		BrokerService broker = BrokerFactory.createBroker(Utilities.getProperty("brokerConfig"), true);
		Utilities.setbrokerURI(broker.getTransportConnectors().get(0).getConnectUri());
		broker.start();
	}
	
	private void makeActiveMqConnection(URI brokerUri) throws JMSException{
		makeActiveMqConnection(brokerUri, null, null);
	}
	private void makeActiveMqConnection(URI brokerUri, String systemUser, String systemPW) throws JMSException{
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUri);
		factory.setUseAsyncSend(true);
		//Use system login account
		if(systemUser!=null)
			factory.setUserName(systemUser);
		if(systemPW!=null)
			factory.setPassword(systemPW);
		connection = (ActiveMQConnection)factory.createConnection();
		connection.start();
	}
	
	public static Connection getConnection() throws NullPointerException{
		if(connection==null)
			throw new NullPointerException("Cannot connect to server. Create GridOPTICSServer instance first.");
		
		return connection;
	}
	
	public void close() throws JMSException {
		if (connection != null) {
			connection.close();
		}
		
		connection = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		//Make really sure that the connection gets closed
		close();
		super.finalize();
	}
	
}
