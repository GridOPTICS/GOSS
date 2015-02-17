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
package pnnl.goss.core.server.internal;

import static pnnl.goss.core.GossCoreContants.PROP_ACTIVEMQ_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;

import java.io.File;
import java.net.URI;
import java.util.Dictionary;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.server.GossRequestHandlerRegistrationService;


public class GridOpticsServer {

    private static final Logger log = LoggerFactory.getLogger(GridOpticsServer.class);

    private static Connection connection;
    private BrokerService broker = null;
    private ServerConsumer consumer = null;

    public GridOpticsServer(GossRequestHandlerRegistrationService handlerService,
        Dictionary<String, Object> coreConfiguration, boolean startBroker) throws Exception{

        Dictionary<String, Object> config = coreConfiguration;
        String brokerURI = (String)config.get(PROP_OPENWIRE_URI);
        URI uri = URI.create(brokerURI);
        String user = (String)config.get(GossCoreContants.PROP_SYSTEM_USER);
        String pw = (String)config.get(GossCoreContants.PROP_SYSTEM_PASSWORD);

        log.debug("Creating gridoptics server\n\tbrokerURI:"+
                brokerURI+"\n\tsystem user: "+user);



        //Needed for standalone server instance
        if(startBroker){
            startBroker(config);
        }

        makeActiveMqConnection(uri, user, pw);

        consumer = new ServerConsumer(coreConfiguration, handlerService);
    }


    private void startBroker(Dictionary<String, Object> config) throws Exception {

        if (config.get(PROP_ACTIVEMQ_CONFIG) != null){
            String brokerConfig = "xbean:" + (String) config.get(PROP_ACTIVEMQ_CONFIG);
            log.debug("Starting broker using config: " + brokerConfig);

            System.setProperty("activemq.base", System.getProperty("user.dir"));
            log.debug("ActiveMQ base directory set as: "+System.getProperty("activemq.base"));
            broker = BrokerFactory.createBroker(brokerConfig, true);
            broker.setDataDirectory(System.getProperty("activemq.base")+"/data");
        }
        else{
            log.debug("Broker started not using xbean "+ (String)config.get(PROP_OPENWIRE_URI));
            broker = new BrokerService();
            broker.addConnector((String)config.get(PROP_OPENWIRE_URI));
            log.warn("Persistent storage is off");
            String datadir = System.getProperty("java.io.tmpdir") + File.separatorChar
                    + "gossdata";
            broker.setDataDirectory(datadir);
            broker.start();
        }
        broker.waitUntilStarted();

    }

    private void makeActiveMqConnection(URI brokerUri, String systemUser, String systemPW) throws JMSException{
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUri);
        factory.setUseAsyncSend(true);
        //Use system login account
        if(systemUser!=null){
            factory.setUserName(systemUser);
        }
        if(systemPW!=null){
            factory.setPassword(systemPW);
        }

        log.debug("Creating connection to: "+brokerUri +" using account: "+ systemUser);
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
        if (consumer != null){
            consumer = null;
        }
        if (broker != null){
            try {
                broker.stop();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        log.debug("Closing connection");
        connection = null;
    }

    @Override
    protected void finalize() throws Throwable {
        //Make really sure that the connection gets closed
        //close();
        super.finalize();
    }

}
