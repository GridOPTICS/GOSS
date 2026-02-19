package pnnl.goss.core.security.jwt;

import java.io.Serializable;
import java.util.Base64;

import jakarta.jms.Destination;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.SimpleAccount;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.security.RoleManager;
import pnnl.goss.core.security.SecurityConfig;

@Component(service = UserRepository.class, configurationPid = "pnnl.goss.core.security.userfile", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class UserRepositoryImpl implements UserRepository {

    @Reference
    private volatile SecurityConfig securityConfig;

    @Reference
    private volatile ClientFactory clientFactory;

    @Reference
    private volatile RoleManager roleManager;

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final String realmName = UnauthTokenBasedRealm.class.getName();
    private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public UserDefault findByUserId(Object userId) {
        return null;
    }

    public UserDefault findById(Object id) {
        return null;
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        log.info("Activating UserRepositoryImpl");
        updated(properties);
        start();
    }

    private void start() {
        try {
            Client client = clientFactory.create(PROTOCOL.STOMP,
                    new UsernamePasswordCredentials(securityConfig.getManagerUser(),
                            securityConfig.getManagerPassword()),
                    false);
            // test publish to make sure the topic exists
            client.publish("ActiveMQ.Advisory.Connection", "");
            String loginTopic = "/topic/" + GossCoreContants.PROP_TOKEN_QUEUE;
            log.info("UserRepositoryImpl subscribing to token topic: {}", loginTopic);
            client.subscribe(loginTopic, new ResponseEvent(client));
        } catch (Exception e) {
            log.error("Error starting UserRepositoryImpl", e);
        }
    }

    @Modified
    public synchronized void updated(Map<String, Object> properties) {

        if (properties != null) {
            log.debug("Updating User Repository Impl");
            userMap.clear();
            userRoles.clear();

            for (String k : properties.keySet()) {
                // Skip OSGi/ConfigAdmin metadata properties
                if (k.startsWith("service.") || k.startsWith("component.") ||
                        k.startsWith("felix.") || k.equals("osgi.ds.satisfying.condition.target")) {
                    continue;
                }

                Object value = properties.get(k);
                if (!(value instanceof String)) {
                    continue;
                }

                String v = (String) value;
                String[] credAndPermissions = v.split(",");
                Set<String> perms = new HashSet<>();
                SimpleAccount acnt = new SimpleAccount(k, credAndPermissions[0], realmName);
                for (int i = 1; i < credAndPermissions.length; i++) {
                    acnt.addStringPermission(credAndPermissions[i]);
                    perms.add(credAndPermissions[i]);
                }
                userMap.put(k, acnt);
                userRoles.put(k, perms);
            }
            log.info("UserRepositoryImpl configured with {} users", userMap.size());
        }

    }

    class ResponseEvent implements GossResponseEvent {
        private final Client client;

        public ResponseEvent(Client client) {
            this.client = client;
        }

        @Override
        public void onMessage(Serializable response) {
            log.debug("Received token request");
            String responseData = "{}";
            if (response instanceof DataResponse) {
                String base64Auth = (String) ((DataResponse) response).getData();
                String userAauthStr = new String(Base64.getDecoder().decode(base64Auth.trim().getBytes()));
                String[] authArr = userAauthStr.split(":");
                String userId = authArr[0];
                // validate submitted username and password before generating token
                if (userMap.containsKey(userId) && authArr[1].equals(userMap.get(userId).getCredentials())) {
                    // Create token
                    String token = null;
                    if (tokenMap.containsKey(userId)) {
                        token = tokenMap.get(userId);
                        log.debug("Token already exists for " + userId);
                    } else {
                        token = securityConfig.createToken(authArr[0], userRoles.get(userId.toString()));
                        log.debug("Created token for " + userId);
                        tokenMap.put(userId, token);
                    }
                    responseData = token;

                } else {
                    log.debug("Authentication failed for " + userId);

                    // Send authentication failed message
                    responseData = "authentication failed";
                }
                Destination replyDest = ((DataResponse) response).getReplyDestination();
                if (replyDest != null) {
                    log.info("Returning token for user " + userId + " on destination " + replyDest);
                    client.publish(replyDest, responseData);
                } else {
                    log.debug("No reply destination for token request from user " + userId + " - ignoring");
                }
            } else {
                client.publish("goss/management/response", responseData);
            }
        }

    }
}
