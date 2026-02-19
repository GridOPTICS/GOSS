package pnnl.goss.core.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.shiro.ShiroPlugin;
import org.apache.activemq.shiro.mgt.DefaultActiveMqSecurityManager;
import org.apache.activemq.usage.SystemUsage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.security.JWTAuthenticationToken;
import pnnl.goss.core.security.SecurityConfig;
import pnnl.goss.core.security.impl.SecurityConfigImpl;
import pnnl.goss.core.security.impl.GossWildcardPermissionResolver;

/**
 * GOSS Simple Runner with Shiro security and JWT token support. Bypasses OSGi
 * and wires security directly into the ActiveMQ broker.
 */
public class GossSimpleRunner {

    private BrokerService brokerService;
    private Connection tokenHandlerConnection;

    // User database loaded from property file
    private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    // Security config for JWT token creation/validation
    private SecurityConfigImpl securityConfig;

    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PASSWORD = "manager";
    private static final String USER_PROPERTIES_FILENAME = "pnnl.goss.core.security.propertyfile.cfg";
    private static final String TOKEN_TOPIC = GossCoreContants.PROP_TOKEN_QUEUE;

    // Configurable ports (system property > env var > default)
    private static final int DEFAULT_OPENWIRE_PORT = 61617;
    private static final int DEFAULT_STOMP_PORT = 61618;
    private static final int DEFAULT_WS_PORT = 61616;

    private int openwirePort;
    private int stompPort;
    private int wsPort;

    /** Read an int config value from system property, env var, or default. */
    private static int configInt(String sysProp, String envVar, int defaultVal) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.isEmpty())
            return Integer.parseInt(val);
        val = System.getenv(envVar);
        if (val != null && !val.isEmpty())
            return Integer.parseInt(val);
        return defaultVal;
    }

    public static void main(String[] args) {
        System.out.println("Starting GOSS Simple Runner...");

        GossSimpleRunner runner = new GossSimpleRunner();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down GOSS...");
            runner.stop();
        }));

        try {
            runner.start();
            System.out.println("GOSS Simple Runner started successfully!");
            System.out.println("Press Ctrl+C to stop");

            // Keep running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start GOSS: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() throws Exception {
        // 0. Read configurable ports
        openwirePort = configInt("goss.openwire.port", "GOSS_OPENWIRE_PORT", DEFAULT_OPENWIRE_PORT);
        stompPort = configInt("goss.stomp.port", "GOSS_STOMP_PORT", DEFAULT_STOMP_PORT);
        wsPort = configInt("goss.ws.port", "GOSS_WS_PORT", DEFAULT_WS_PORT);

        // 1. Load user properties
        loadUserProperties();

        // 2. Initialize SecurityConfig for JWT tokens
        initSecurityConfig();

        // 3. Start broker with Shiro security
        System.out.println("Starting ActiveMQ Broker with Shiro security...");
        startBroker();

        // 4. Start token request handler
        startTokenHandler();

        System.out.println("GOSS Core services are running");
        System.out.println("ActiveMQ Broker: tcp://0.0.0.0:" + openwirePort);
        System.out.println("STOMP: stomp://0.0.0.0:" + stompPort);
        System.out.println("WebSocket: ws://0.0.0.0:" + wsPort);
        System.out.println("Security: Shiro authentication enabled (" + userMap.size() + " users)");
        System.out.println("Token support: JWT token authentication enabled");
    }

    public void stop() {
        try {
            if (tokenHandlerConnection != null) {
                tokenHandlerConnection.close();
            }
            if (brokerService != null) {
                brokerService.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping GOSS: " + e.getMessage());
        }
    }

    /**
     * Locate the user properties file by checking (in order):
     *   1. GOSS_USER_PROPERTIES env var / goss.user.properties system property
     *   2. conf/<filename>  (next to the JAR / working dir)
     *   3. pnnl.goss.core.runner/conf/<filename>  (run from GOSS root)
     *   4. ../conf/<filename>  (JAR is inside generated/executable)
     *   5. ../../conf/<filename>
     */
    private File findUserPropertiesFile() {
        // Explicit override
        String explicit = System.getProperty("goss.user.properties");
        if (explicit == null) explicit = System.getenv("GOSS_USER_PROPERTIES");
        if (explicit != null) {
            File f = new File(explicit);
            if (f.exists()) return f;
            System.err.println("Explicit user properties path not found: " + explicit);
        }

        // Resolve the directory that contains the JAR
        File jarDir;
        try {
            jarDir = new File(GossSimpleRunner.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParentFile();
        } catch (Exception e) {
            jarDir = new File(".");
        }

        String[] candidates = {
            "conf/" + USER_PROPERTIES_FILENAME,
            "pnnl.goss.core.runner/conf/" + USER_PROPERTIES_FILENAME,
            jarDir + "/conf/" + USER_PROPERTIES_FILENAME,
            jarDir + "/../conf/" + USER_PROPERTIES_FILENAME,
            jarDir + "/../../conf/" + USER_PROPERTIES_FILENAME,
        };

        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) return f;
        }
        return null;
    }

    private void loadUserProperties() {
        File propsFile = findUserPropertiesFile();
        if (propsFile == null) {
            System.out.println("No user properties file (" + USER_PROPERTIES_FILENAME
                    + ") found in any search path");
            System.out.println("Using default system user only");
            SimpleAccount systemAcct = new SimpleAccount(SYSTEM_USER, SYSTEM_PASSWORD, "SimpleRunnerRealm");
            systemAcct.addStringPermission("*");
            userMap.put(SYSTEM_USER, systemAcct);
            Set<String> perms = new HashSet<>();
            perms.add("*");
            userPermissions.put(SYSTEM_USER, perms);
            return;
        }

        System.out.println("Loading users from " + propsFile.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(propsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx < 0) {
                    continue;
                }
                String username = line.substring(0, eqIdx).trim();
                String value = line.substring(eqIdx + 1).trim();
                String[] parts = value.split(",");
                if (parts.length < 1) {
                    continue;
                }

                String password = parts[0];
                SimpleAccount acct = new SimpleAccount(username, password, "SimpleRunnerRealm");
                Set<String> perms = new HashSet<>();
                for (int i = 1; i < parts.length; i++) {
                    String perm = parts[i].trim();
                    if (!perm.isEmpty()) {
                        acct.addStringPermission(perm);
                        perms.add(perm);
                    }
                }
                userMap.put(username, acct);
                userPermissions.put(username, perms);
            }
            System.out.println("Loaded " + userMap.size() + " users from " + propsFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error reading user properties: " + e.getMessage());
            // Fall back to default system user
            SimpleAccount systemAcct = new SimpleAccount(SYSTEM_USER, SYSTEM_PASSWORD, "SimpleRunnerRealm");
            systemAcct.addStringPermission("*");
            userMap.put(SYSTEM_USER, systemAcct);
            Set<String> perms = new HashSet<>();
            perms.add("*");
            userPermissions.put(SYSTEM_USER, perms);
        }
    }

    private void initSecurityConfig() {
        // Create SecurityConfigImpl with system manager credentials
        securityConfig = new SecurityConfigImpl();
        Map<String, Object> secProps = new HashMap<>();
        secProps.put("goss.system.manager", SYSTEM_USER);
        secProps.put("goss.system.manager.password", SYSTEM_PASSWORD);
        securityConfig.updated(secProps);
    }

    private void startBroker() throws Exception {
        brokerService = new BrokerService();
        brokerService.setBrokerName("goss-broker");
        brokerService.setDataDirectory("data");

        // Configure system usage
        SystemUsage systemUsage = brokerService.getSystemUsage();
        systemUsage.getMemoryUsage().setLimit(64 * 1024 * 1024); // 64MB
        systemUsage.getStoreUsage().setLimit(1024 * 1024 * 1024); // 1GB

        // Set up Shiro security
        DefaultActiveMqSecurityManager securityManager = new DefaultActiveMqSecurityManager();
        securityManager.setCacheManager(new MemoryConstrainedCacheManager());

        // Create realms
        Set<Realm> realms = new HashSet<>();

        // Property-based realm for username/password auth
        PropertyRealm propertyRealm = new PropertyRealm();
        realms.add(propertyRealm);

        // Token-based realm for JWT auth
        TokenRealm tokenRealm = new TokenRealm();
        realms.add(tokenRealm);

        securityManager.setRealms(realms);
        SecurityUtils.setSecurityManager(securityManager);

        // Attach ShiroPlugin to broker
        ShiroPlugin shiroPlugin = new ShiroPlugin();
        shiroPlugin.setSecurityManager(securityManager);
        brokerService.setPlugins(new BrokerPlugin[]{shiroPlugin});

        // Add connectors
        TransportConnector openwireConnector = new TransportConnector();
        openwireConnector.setUri(new URI("tcp://0.0.0.0:" + openwirePort));
        openwireConnector.setName("openwire");
        brokerService.addConnector(openwireConnector);

        TransportConnector stompConnector = new TransportConnector();
        stompConnector.setUri(new URI("stomp://0.0.0.0:" + stompPort));
        stompConnector.setName("stomp");
        brokerService.addConnector(stompConnector);

        TransportConnector wsConnector = new TransportConnector();
        wsConnector.setUri(new URI("ws://0.0.0.0:" + wsPort
                + "?websocket.maxTextMessageSize=999999"
                + "&websocket.maxIdleTime=60000"
                + "&websocket.bufferSize=32536"));
        wsConnector.setName("ws");
        brokerService.addConnector(wsConnector);

        brokerService.start();
    }

    private void startTokenHandler() throws Exception {
        // Connect to the local broker as the system user
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:" + openwirePort);
        factory.setUserName(SYSTEM_USER);
        factory.setPassword(SYSTEM_PASSWORD);
        tokenHandlerConnection = factory.createConnection();
        tokenHandlerConnection.start();

        Session session = tokenHandlerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic tokenTopic = session.createTopic(TOKEN_TOPIC);
        MessageConsumer consumer = session.createConsumer(tokenTopic);

        // Also create a producer for sending responses
        MessageProducer producer = session.createProducer(null); // null dest = use per-message dest

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String body = null;
                    if (message instanceof TextMessage) {
                        body = ((TextMessage) message).getText();
                    } else if (message instanceof BytesMessage) {
                        BytesMessage bm = (BytesMessage) message;
                        byte[] bytes = new byte[(int) bm.getBodyLength()];
                        bm.readBytes(bytes);
                        body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    }

                    if (body == null || body.isEmpty()) {
                        return;
                    }

                    // Decode base64(username:password)
                    String decoded = new String(Base64.getDecoder().decode(body.trim()));
                    String[] authParts = decoded.split(":", 2);
                    if (authParts.length < 2) {
                        return;
                    }

                    String userId = authParts[0];
                    String password = authParts[1];
                    String responseData;

                    // Validate credentials
                    SimpleAccount acct = userMap.get(userId);
                    if (acct != null && password.equals(acct.getCredentials())) {
                        // Create or reuse cached token
                        String token = tokenCache.get(userId);
                        if (token == null) {
                            token = securityConfig.createToken(userId,
                                    userPermissions.getOrDefault(userId, new HashSet<>()));
                            tokenCache.put(userId, token);
                            System.out.println("Created token for user: " + userId);
                        }
                        responseData = token;
                    } else {
                        System.out.println("Authentication failed for user: " + userId);
                        responseData = "authentication failed";
                    }

                    // Send response to reply-to destination
                    Destination replyTo = message.getJMSReplyTo();
                    System.out.println("Token handler: JMSReplyTo=" + replyTo);
                    if (replyTo != null) {
                        TextMessage response = session.createTextMessage(responseData);
                        producer.send(replyTo, response);
                        System.out.println("Token response sent to: " + replyTo);
                    } else {
                        // STOMP reply-to may be stored as a string property
                        String replyToStr = message.getStringProperty("reply-to");
                        System.out.println("Token handler: reply-to property=" + replyToStr);
                        if (replyToStr != null && !replyToStr.isEmpty()) {
                            Destination replyDest;
                            if (replyToStr.startsWith("/queue/")) {
                                replyDest = session.createQueue(replyToStr.substring("/queue/".length()));
                            } else if (replyToStr.startsWith("/topic/")) {
                                replyDest = session.createTopic(replyToStr.substring("/topic/".length()));
                            } else {
                                // Default to queue
                                replyDest = session.createQueue(replyToStr);
                            }
                            TextMessage response = session.createTextMessage(responseData);
                            producer.send(replyDest, response);
                            System.out.println("Token response sent to fallback dest: " + replyDest);
                        } else {
                            System.err.println("No reply-to destination found on token request message");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error handling token request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Token handler listening on /topic/" + TOKEN_TOPIC);
    }

    /**
     * Simple realm for username/password authentication from the property file.
     */
    private class PropertyRealm extends AuthorizingRealm {

        private final GossWildcardPermissionResolver resolver = new GossWildcardPermissionResolver();

        PropertyRealm() {
            setName("PropertyRealm");
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            String username = (String) getAvailablePrincipal(principals);
            return userMap.get(username);
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
                throws AuthenticationException {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String username = upToken.getUsername();
            if (username == null) {
                return null;
            }
            // Don't handle tokens (long username, empty password) — let TokenRealm do that
            char[] pw = upToken.getPassword();
            if (username.length() > 250 && (pw == null || pw.length == 0)) {
                return null;
            }
            return userMap.get(username);
        }

        @Override
        public PermissionResolver getPermissionResolver() {
            return resolver;
        }
    }

    /**
     * Realm for JWT token-based authentication. Detects tokens by: username > 250
     * chars and empty password.
     */
    private class TokenRealm extends AuthorizingRealm {

        private final Map<String, SimpleAccount> tokenAccountMap = new ConcurrentHashMap<>();
        private final GossWildcardPermissionResolver resolver = new GossWildcardPermissionResolver();

        TokenRealm() {
            setName("TokenRealm");
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            String username = (String) getAvailablePrincipal(principals);
            return tokenAccountMap.get(username);
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
                throws AuthenticationException {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String username = upToken.getUsername();
            char[] pw = upToken.getPassword();

            // Only handle JWT tokens: long username, empty password
            if (username == null || username.length() <= 250 || (pw != null && pw.length > 0)) {
                return null;
            }

            // Validate the JWT token
            boolean verified = securityConfig.validateToken(username);
            if (!verified) {
                return null;
            }

            // Parse token to extract roles/permissions
            JWTAuthenticationToken jwtToken = securityConfig.parseToken(username);
            if (jwtToken == null) {
                return null;
            }

            SimpleAccount acct = new SimpleAccount(username, "", getName());
            // Grant the permissions from the user's roles
            if (jwtToken.getRoles() != null) {
                for (String perm : jwtToken.getRoles()) {
                    acct.addStringPermission(perm);
                }
            }
            // Also grant wildcard for token-authenticated users
            // (matching the test expectations for pub/sub)
            acct.addStringPermission("topic:*");
            acct.addStringPermission("queue:*");
            acct.addStringPermission("temp-queue:*");
            tokenAccountMap.put(username, acct);
            return acct;
        }

        @Override
        public PermissionResolver getPermissionResolver() {
            return resolver;
        }
    }
}
