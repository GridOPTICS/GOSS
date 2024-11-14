package pnnl.goss.core.server.plugins;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ConsumerBrokerExchange;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.RemoveInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionMonitoringPlugin implements BrokerPlugin {

    private static final Logger log = LoggerFactory.getLogger(ConnectionMonitoringPlugin.class);

    @Override
    public Broker installPlugin(Broker broker) {
        return new BrokerFilter(broker) {

            @Override
            public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
                super.addConnection(context, info);
                log.info("Client connected: " + info.getClientId());
                // Additional logic on client connect
            }

            @Override
            public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error) throws Exception {
                super.removeConnection(context, info, error);
                log.info("Client disconnected: " + info.getClientId());
                // Additional logic on client disconnect
                
            }

            // Implement other methods as needed (optional)
            @Override
            public void send(ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {
            	if (messageSend.getDestination().equals("topic://goss/system/tick")){
            		log.trace(messageSend.toString());
            	}
            	else {
            		log.debug("send: producerExchange: " + producerExchange.getConnectionContext().getClientId() + "\n\tmessage: " + messageSend.toString());
            	}
            	            	
                super.send(producerExchange, messageSend);
            }

            @Override
            public void acknowledge(ConsumerBrokerExchange consumerExchange, MessageAck ack) throws Exception {
            	log.info("Client acknowledge: " + ack.toString());
                super.acknowledge(consumerExchange, ack);
            }

        };
    }
}
