package pnnl.goss.activemq.testing;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslBrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.activemq.transport.TransportBrokerTestSupport;

public class ActiveMQSslConnectionFactoryTest  {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQSslConnectionFactoryTest.class);

    

    public static final String KEYSTORE_TYPE = "jks";
    public static final String PASSWORD = "password";
    public static final String SERVER_KS_PASSWORD = "GossServerTemp";
    public static final String CLIENT_KS_PASSWORD = "GossClientTemp";
    public static final String SERVER_TS_PASSWORD = "GossServerTrust";
    public static final String CLIENT_TS_PASSWORD = "GossClientTrust";
    
    //public static final String PASSWORD = "password";
    public static final String SERVER_KEYSTORE = "resources/keystores/mybroker.ks";
    public static final String SERVER_TRUSTSTORE = "resources/keystores/mybroker.ts";
    public static final String CLIENT_KEYSTORE = "resources/keystores/myclient.ks";
    public static final String CLIENT_TRUSTSTORE = "resources/keystores/myclient.ts";

    private TransportConnector connector;
    private ActiveMQConnection connection;
    private BrokerService broker;

    @After
    public void tearDown() throws Exception {
        // Try our best to close any previously opend connection.
        try {
            connection.close();
        } catch (Throwable ignore) {
        }
        // Try our best to stop any previously started broker.
        try {
            broker.stop();
        } catch (Throwable ignore) {
        }
    }
   
    @Test
    public void testCreateTcpConnectionUsingKnownPort() throws Exception {
        // Control case: check that the factory can create an ordinary (non-ssl) connection.
        broker = createBroker("tcp://localhost:61610?wireFormat.tcpNoDelayEnabled=true");

        // This should create the connection.
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61610?wireFormat.tcpNoDelayEnabled=true");
        connection = (ActiveMQConnection)cf.createConnection();
        assertNotNull(connection);

        brokerStop();
    }

    @Test
    public void testCreateSslConnection() throws Exception {
        // Create SSL/TLS connection with trusted cert from truststore.
    	System.out.println(System.getProperty("user.dir"));
    	String sslUri = "ssl://localhost:61611";
        broker = createSslBroker(sslUri);
        assertNotNull(broker);

        // This should create the connection.
        ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(sslUri);
        cf.setTrustStore(CLIENT_TRUSTSTORE);
        cf.setTrustStorePassword(CLIENT_TS_PASSWORD);
        connection = (ActiveMQConnection)cf.createConnection();
        LOG.info("Created client connection");
        assertNotNull(connection);

        brokerStop();
    }

    @Test
    public void testNegativeCreateSslConnectionWithWrongPassword() throws Exception {
        // Create SSL/TLS connection with trusted cert from truststore.
    	String sslUri = "ssl://localhost:61611";
        broker = createSslBroker(sslUri);
        assertNotNull(broker);

        // This should FAIL to connect, due to wrong password.
        ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(sslUri);
        cf.setTrustStore(CLIENT_TRUSTSTORE);
        cf.setTrustStorePassword("wrongPassword");
        try {
            connection = (ActiveMQConnection)cf.createConnection();
        }
        catch (javax.jms.JMSException ignore) {
        	// Expected exception
        	LOG.info("Expected java.io.Exception [" + ignore + "]");
        }
        assertNull(connection);

        brokerStop();
    }

    @Test
    public void testNegativeCreateSslConnectionWithWrongCert() throws Exception {
        // Create SSL/TLS connection with trusted cert from truststore.
    	String sslUri = "ssl://localhost:61611";
        broker = createSslBroker(sslUri);
        assertNotNull(broker);

        // This should FAIL to connect, due to wrong password.
        ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(sslUri);
        cf.setTrustStore("dummy.keystore");
        cf.setTrustStorePassword("password");
        try {
            connection = (ActiveMQConnection)cf.createConnection();
        }
        catch (javax.jms.JMSException ignore) {
        	// Expected exception
        	LOG.info("Expected SSLHandshakeException [" + ignore + "]");
        }
        assertNull(connection);

        brokerStop();
    }

    protected BrokerService createBroker(String uri) throws Exception {
        // Start up a broker with a tcp connector.
        BrokerService service = new BrokerService();
        service.setPersistent(false);
        connector = service.addConnector(uri);
        service.start();

        return service;
    }

    protected BrokerService createSslBroker(String uri) throws Exception {
        
        // http://java.sun.com/javase/javaseforbusiness/docs/TLSReadme.html
        // work around: javax.net.ssl.SSLHandshakeException: renegotiation is not allowed
        //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        
        SslBrokerService service = new SslBrokerService();
        service.setPersistent(false);
        
        KeyManager[] km = getKeyManager();
        TrustManager[] tm = getTrustManager();
        connector = service.addSslConnector(uri, km, tm, null);
        service.start();
        
        return service;
    }

    protected void brokerStop() throws Exception {
        broker.stop();
    }

    public static TrustManager[] getTrustManager() throws Exception {
        TrustManager[] trustStoreManagers = null;
        KeyStore trustedCertStore = KeyStore.getInstance(ActiveMQSslConnectionFactoryTest.KEYSTORE_TYPE);
        
        trustedCertStore.load(new FileInputStream(ActiveMQSslConnectionFactoryTest.CLIENT_TRUSTSTORE), null);
        TrustManagerFactory tmf  = 
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
  
        tmf.init(trustedCertStore);
        trustStoreManagers = tmf.getTrustManagers();
        return trustStoreManagers; 
    }

    public static KeyManager[] getKeyManager() throws Exception {
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());  
        KeyStore ks = KeyStore.getInstance(ActiveMQSslConnectionFactoryTest.KEYSTORE_TYPE);
        KeyManager[] keystoreManagers = null;
        
        byte[] sslCert = loadClientCredential(ActiveMQSslConnectionFactoryTest.SERVER_KEYSTORE);
        
       
        if (sslCert != null && sslCert.length > 0) {
            ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
            ks.load(bin, ActiveMQSslConnectionFactoryTest.SERVER_KS_PASSWORD.toCharArray());
            kmf.init(ks, ActiveMQSslConnectionFactoryTest.SERVER_KS_PASSWORD.toCharArray());
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
    }}
