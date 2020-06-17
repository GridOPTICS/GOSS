package pnnl.goss.core.security.jwt;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.security.GossPermissionResolver;
import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.RoleManager;
import pnnl.goss.core.security.SecurityConfig;


/**
 * This class handles property based authentication/authorization.  It will only be
 * started as a component if a pnnl.goss.core.security.properties.cfg file exists
 * within the configuration directory.
 * 
 * The format of each property should be username=password,permission1,permission2 ... where
 * permission1 and permission2 are of the format domain:object:action.  There can be multiple
 * levels of domain object and action.  An example permission string format is printers:lp2def:create
 * or topic:request:subscribe.
 * 
 * NOTE: This class assumes uniqueness of username in the properties file.
 * 
 * @author Craig Allwardt
 *
 */
@Component
public class UnauthTokenBasedRealm extends AuthorizingRealm implements GossRealm {
	
	private static final String CONFIG_PID = "pnnl.goss.core.security.unauthrealm";
	private static final Logger log = LoggerFactory.getLogger(UnauthTokenBasedRealm.class);
	
	private final Map<String, SimpleAccount> tokenMap = new ConcurrentHashMap<>();
//	private final Map<String, Set<String>> tokenPermissions = new ConcurrentHashMap<>();
	
	@ServiceDependency
	GossPermissionResolver gossPermissionResolver;
	
	@ServiceDependency
    private volatile SecurityConfig securityConfig;
	
	@ServiceDependency 
	private volatile UserRepository userRepository;
	
	@ServiceDependency
	private volatile RoleManager roleManager;
	
	@ConfigurationDependency(pid=CONFIG_PID)
	public synchronized void updated(Dictionary<String, ?> properties) throws SystemException {
		if (properties != null) {
//			Enumeration<String> keys = properties.keys();
			
//			while(keys.hasMoreElements()){
//				String user = keys.nextElement();
//				String groups = properties.get(user).toString();
//				System.out.println("Registering user roles: "+user+" --  "+groups);
//				List<String> groupList = new ArrayList(Arrays.asList(StringUtils.split(groups, ',')));
//				//TODO in RIGHT HERE
//				roles.put(user, groupList);
//			}
		}
		
		
		System.out.println("UPDATING UNAUTH REALM");
//		Set<String> perms = new HashSet<>();
//		System.out.println("UNATH MANAGER IN SYSTEM REALM "+securityConfig);
////		SimpleAccount acnt = new SimpleAccount(securityConfig.getManagerUser(), securityConfig.getManagerPassword(), getName() );
//		SimpleAccount acnt = new SimpleAccount("token", "token", getName() );
//		acnt.addStringPermission("queue:*");
//		acnt.addStringPermission("topic:*");
//		acnt.addStringPermission("temp-queue:*");
//		acnt.addStringPermission("fusion:*:read");
//		acnt.addStringPermission("fusion:*:write");
//		tokenMap.put("token", acnt);
//		tokenPermissions.put("token", perms);
	}
	
	@Start
	public void start(){
		System.out.println("IN START FOR UNAUTH AUTH REALM");
	}
	
//	@Override
//	protected void onInit() {
//			super.onInit();
//			Set<String> perms = new HashSet<>();
//
//			System.out.println("UNATH MANAGER IN SYSTEM REALM "+securityConfig);
////			SimpleAccount acnt = new SimpleAccount(securityConfig.getManagerUser(), securityConfig.getManagerPassword(), getName() );
//			SimpleAccount acnt = new SimpleAccount("token", "token", getName() );
//			acnt.addStringPermission("queue:*");
//			acnt.addStringPermission("topic:*");
//			acnt.addStringPermission("temp-queue:*");
//			acnt.addStringPermission("fusion:*:read");
//			acnt.addStringPermission("fusion:*:write");
//			userMap.put("token", acnt);
//			userPermissions.put("token", perms);
//		}
	
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		//get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        AuthorizationInfo accnt = tokenMap.get(username);
        if(!username.equals("system")){
        	System.out.println("UNAUTH GET AUTHZ "+username+"  "+accnt);
//        	try{
//        		throw new Exception("in authz");
//        	} catch (Exception e) {
//        		e.printStackTrace();
//				// TODO: handle exception
//			}
        	
        }
        
        return accnt;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
//		try {
//			throw new Exception("in get authn");
//		} catch (Exception e) {
//			e.printStackTrace();
//			// TODO: handle exception
//		}
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
//        upToken.setRememberMe(true);
        SimpleAccount acnt = null;
        String username = upToken.getUsername();
		System.out.println("DO GET AUTH INFO UNAUTH "+token+" user "+username);

        char[] pw = upToken.getPassword();
        //If it receives a token
        if (username!=null && username.length()>250 && pw.length==0) {
        	//Validate token
        	System.out.println("HAS TOKEN, VERIFYING");
        	boolean verified = userRepository.validateToken(username);
        	System.out.println("IS VERIFIED "+verified); 
        	if(verified){
        	//TODO get username from token, get permissions for username
        		SignedJWT signed;
				try {
					signed = SignedJWT.parse(username);
					System.out.println("SIGNED "+signed+" "+signed.getParsedString());
					Payload payload = signed.getPayload();
					String jsonToken = payload.toJSONObject().toJSONString();
					System.out.println("GOT TOKEN PAYLOAD "+jsonToken);
					//TODO look up permissions based on roles and add them
					Set<String> permissions = new HashSet<String>();
					JWTAuthenticationToken tokenObj = JWTAuthenticationToken.parse(jsonToken);
					if(roleManager!=null){
						permissions = roleManager.getRolePermissions(tokenObj.getRoles());
						System.out.println("PERMISSIONS FOR TOKEN "+permissions);
					}else {
						System.out.println("ROLE MGR IS NULL!!!");
					}
					acnt = new SimpleAccount(username, "", getName() );
					for(String perm: permissions){
						acnt.addStringPermission(perm);
					}
					tokenMap.put(username, acnt);
//					tokenPermissions.put(username, permissions);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

        	}
            
             
        } else {
        	//Only let it past if it is coming from the "token" account
//	        if(!"token".equals(upToken.getUsername()) && !"token".equals(upToken.getPassword())){
//	        	System.out.println("NOT TOKEN USER "+upToken.getUsername()+" "+upToken.getPassword());
//	        	return null;
//	        }
        	if("system".equals(upToken.getUsername()) ){
        		return null;
        	}
        	
        	
//	        if("token".equals(upToken.getUsername()) && "token".equals(upToken.getPassword().toString())){
//	        if("token".equals(upToken.getUsername()) ){
//	        	String pwStr = upToken.getPassword().toString();
        	String userName = upToken.getUsername();
        	//todo check usenamr and pw against user repository
        	String loginTopic = "/topic/"+GossCoreContants.PROP_TOKEN_QUEUE;
//	        		Set<String> permissions = new HashSet<String>();
        	acnt = new SimpleAccount(upToken.getUsername(), upToken.getPassword(), getName() );
//				acnt.addStringPermission("queue:*");
//				acnt.addStringPermission("topic:"+loginTopic);
//				acnt.addStringPermission("topic:"+"ActiveMQ.Advisory.Connection");
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Connection:create");
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Queue:create");
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Consumer.Queue.temp.token_resp."+userName);
        	acnt.addStringPermission("topic:"+GossCoreContants.PROP_TOKEN_QUEUE+":write");
        	acnt.addStringPermission("topic:"+GossCoreContants.PROP_TOKEN_QUEUE+":create");
        	acnt.addStringPermission("queue:temp.token_resp."+userName);

//				permissions.add("topic:"+loginTopic);
//				acnt.addStringPermission("temp-queue:*");
//				acnt.addStringPermission("fusion:*:read");
//				acnt.addStringPermission("fusion:*:write");

        	tokenMap.put(username, acnt);
//				tokenPermissions.put(username, permissions);
				
//        	return acnt;
//	        }
        }
		return acnt;
	}
	

	@Override
	public Set<String> getPermissions(String identifier) {
		if(!identifier.equals("system")){
        	System.out.println("GET PERMS "+identifier+"  ");
//		try{
//			throw new Exception("here");
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
		}
//		if (hasIdentifier(identifier)){
//			System.out.println(tokenPermissions.get(identifier));
//			return tokenPermissions.get(identifier);
//		}
		return new HashSet<>();
	}

	@Override
	public boolean hasIdentifier(String identifier) {
		return tokenMap.containsKey(identifier);
	}
	
	 @Override
	public PermissionResolver getPermissionResolver() {
		 if(gossPermissionResolver!=null)
			 return gossPermissionResolver;
		 else 
			 return super.getPermissionResolver();
	}
}