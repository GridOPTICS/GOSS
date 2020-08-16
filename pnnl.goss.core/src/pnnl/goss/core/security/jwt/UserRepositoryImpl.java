package pnnl.goss.core.security.jwt;

//import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.security.RoleManager;
import pnnl.goss.core.security.SecurityConfig;

import java.io.Serializable;

//import pnnl.goss.core.Client;
//import pnnl.goss.core.Client.PROTOCOL;
//import pnnl.goss.core.ClientFactory;
//import pnnl.goss.core.GossCoreContants;
//import pnnl.goss.core.GossResponseEvent;
//import pnnl.goss.core.security.SecurityConfig;
//import pnnl.goss.core.security.UserRepository;

//import java.io.Serializable;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.felix.dm.annotation.api.ServiceDependency;
//import org.apache.http.auth.Credentials;
//import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.util.StringUtils;
//import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UserRepositoryImpl implements UserRepository{
	
	@ServiceDependency
	private volatile SecurityConfig securityConfig;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	@ServiceDependency
	private volatile RoleManager roleManager;
	
	
	//These should probably come from config
	private static final String ISSUED_BY = "GridOPTICS Software System";
	private byte[] sharedKey = generateSharedKey();
	
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.userfile";
	private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
	private final String realmName = UnauthTokenBasedRealm.class.getName();//JWTRealm.class.getName();
	private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
//	private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();
	

    public UserDefault findByUserId(Object userId){return null;}

    public UserDefault findById(Object id){return null;}

    public byte[] generateSharedKey() {
        SecureRandom random = new SecureRandom();
        byte[] sharedKey = new byte[32];
        random.nextBytes(sharedKey);
        return sharedKey;
    }

    public long getExpirationDate() {
        return 1000 * 60 * 60 * 24 * 5;
    }

    public String getIssuer(){return ISSUED_BY;}

    private byte[] getSharedKey(){return sharedKey;}

//    public TokenResponse createToken(UserDefault user) {
//        TokenResponse response = new TokenResponse(user, createToken(user.getPrincipal()));
//        return response;
//    }

    public String createToken(Object userId) {
    	log.info("Creating token for user "+userId);
        try {
        	//TODO, should also include roles(permissions)
        	
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
            builder.issuer(getIssuer());
            builder.subject(userId.toString());
            builder.issueTime(new Date());
            builder.notBeforeTime(new Date());
            builder.expirationTime(new Date(new Date().getTime() + getExpirationDate()));
            builder.jwtID(UUID.randomUUID().toString());
            
            JWTAuthenticationToken tokenObj = new JWTAuthenticationToken();
            tokenObj.setIss(getIssuer());
            tokenObj.setSub(userId.toString());
            tokenObj.setIat(new Date().getTime());
            tokenObj.setNbf(new Date().getTime());
            tokenObj.setExp(new Date(new Date().getTime() + getExpirationDate()).getTime());
            tokenObj.setJti(UUID.randomUUID().toString());
            //TODO GET ROLES
            Set<String> roles = userRoles.get(userId.toString());
            tokenObj.setRoles(new ArrayList<String>(roles));
            Payload payload = new Payload(tokenObj.toString());

//            JWTClaimsSet claimsSet = builder.build();
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(header, payload);

            JWSSigner signer = new MACSigner(getSharedKey());
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (JOSEException ex) {
            return null;
        }
    }

    
    public boolean validateToken(String token) {
    	log.debug("Validate token "+token);
        try {
            SignedJWT signed = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifierExtended(getSharedKey(), signed.getJWTClaimsSet());
            boolean verified = signed.verify(verifier);
            log.debug("Verified: "+verified);
            return verified;
        } catch (ParseException ex) {
            return false;
        } catch (JOSEException ex) {
            return false;
        }

    }
    
    
    @Start
    public void start(){
		try {
			Client client = clientFactory.create(PROTOCOL.STOMP,
					new UsernamePasswordCredentials(securityConfig.getManagerUser(), securityConfig.getManagerPassword()));
			//test publish to make sure the topic exists
			client.publish("ActiveMQ.Advisory.Connection", "");
			String loginTopic = "/topic/"+GossCoreContants.PROP_TOKEN_QUEUE;
			client.subscribe(loginTopic,  new ResponseEvent(client));
		}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
    	

		if (properties != null){
			log.debug("Updating User Repository Impl");
			userMap.clear();
			userRoles.clear();
			
			Enumeration<String> keys = properties.keys();
			
			while(keys.hasMoreElements()){
				String k = keys.nextElement();
				String v = (String)properties.get(k);
				String[] credAndPermissions = v.split(",");
				Set<String> perms = new HashSet<>();
				SimpleAccount acnt = new SimpleAccount(k, credAndPermissions[0], realmName);
				for(int i =1; i<credAndPermissions.length; i++){
					acnt.addStringPermission(credAndPermissions[i]);
					perms.add(credAndPermissions[i]);
				}
				userMap.put(k, acnt);
				userRoles.put(k, perms);
				
			}
			
		}		
		
		
    }
		
		




class ResponseEvent implements GossResponseEvent{
	private final Client client;
//	private Gson gson = new Gson();

	public ResponseEvent(Client client){
		this.client = client;
	}

	@Override
	public void onMessage(Serializable response) {
		log.debug("Received token request");
		String responseData = "{}";
		if (response instanceof DataResponse){
			String base64Auth = (String)((DataResponse) response).getData();
			String userAauthStr = new String(Base64.getDecoder().decode(base64Auth.trim().getBytes()));
			String[] authArr = userAauthStr.split(":");
			String userId = authArr[0];
			//validate submitted username and password before generating token
			if(userMap.containsKey(userId) && authArr[1].equals(userMap.get(userId).getCredentials())){
				//Create token
				String token = createToken(authArr[0]);
				log.debug("Created token for "+userId);
				responseData = token;
				
			} else {
				log.debug("Authentication failed for "+userId);

				//Send authentication failed message
				responseData = "authentication failed";
			}
	    	log.info("Returning token for user "+userId+" on destination "+((DataResponse) response).getReplyDestination());

			client.publish(((DataResponse) response).getReplyDestination(), responseData);
		} else {
//			System.out.println("On message: "+response.toString());
			client.publish("goss/management/response", responseData);
		}
	}

}

}
