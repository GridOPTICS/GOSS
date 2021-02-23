package pnnl.goss.core.security.jwt;

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

//import java.io.Serializable;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.authc.SimpleAccount;
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
//	private static final String ISSUED_BY = "GridOPTICS Software System";
//	private byte[] sharedKey = securityConfig.getTokenSecret();  
//	private byte[] sharedKey = generateSharedKey();
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.userfile";
	private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
	private final String realmName = UnauthTokenBasedRealm.class.getName();//JWTRealm.class.getName();
	private final Map<String, SimpleAccount> userMap = new ConcurrentHashMap<>();
//	private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();
	private final Map<String, String> tokenMap = new ConcurrentHashMap<>();


    public UserDefault findByUserId(Object userId){return null;}

    public UserDefault findById(Object id){return null;}



    
//    private byte[] getSharedKey(){return sharedKey;}

//    public TokenResponse createToken(UserDefault user) {
//        TokenResponse response = new TokenResponse(user, createToken(user.getPrincipal()));
//        return response;
//    }

   

    
   
    
    
    @Start
    public void start(){
		try {
			Client client = clientFactory.create(PROTOCOL.STOMP,
					new UsernamePasswordCredentials(securityConfig.getManagerUser(), securityConfig.getManagerPassword()), false);
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
				System.out.println("USER MATCHES FOR "+userId);
				//Create token
				String token = null;
				if(tokenMap.containsKey(userId)){
					token=tokenMap.get(userId);
					System.out.println("TOKEN ALREADY EXISTS FOR "+userId+"   "+token);
	        		log.debug("Token already exists for "+userId);
	        	} else {
	        		token = securityConfig.createToken(authArr[0], userRoles.get(userId.toString()));
	        		System.out.println("GENERATED TOKEN FOR "+userId+"    "+token);
	        		log.debug("Created token for "+userId);
	        		tokenMap.put(userId, token);
	        	}
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
//	private byte[] generateSharedKey() {
//	    SecureRandom random = new SecureRandom();
//	    byte[] sharedKey = new byte[32];
//	    random.nextBytes(sharedKey);
//	    return sharedKey;
//	}
}
