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
	}
	
	
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		//get the principal this realm cares about:
        String username = (String) getAvailablePrincipal(principals);
        AuthorizationInfo accnt = tokenMap.get(username);
        if(accnt==null){
        	log.debug("No authrorization info found for "+username);
        }
        return accnt;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
        //objects.  See the Realm.supports() method if your application will use a different type of token.
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
//        upToken.setRememberMe(true);
        SimpleAccount acnt = null;
        String username = upToken.getUsername();
		log.info("Get authentication info for "+username);

        char[] pw = upToken.getPassword();
        //If it receives a token
        if (username!=null && username.length()>250 && pw.length==0) {
        	//Validate token
        	boolean verified = userRepository.validateToken(username);
        	log.info("Recieved token: "+username+"  verified: "+verified);
        	if(verified){
        	//TODO get username from token, get permissions for username
        		
        		SignedJWT signed;
				try {
					signed = SignedJWT.parse(username);
					Payload payload = signed.getPayload();
					String jsonToken = payload.toJSONObject().toJSONString();
					
					// look up permissions based on roles and add them
					Set<String> permissions = new HashSet<String>();
					JWTAuthenticationToken tokenObj = JWTAuthenticationToken.parse(jsonToken);
		        	log.info("Has token roles: "+tokenObj.getRoles());

					if(roleManager!=null){
						permissions = roleManager.getRolePermissions(tokenObj.getRoles());
						log.debug("Permissions for user "+username+": "+permissions);
					}else {
						log.warn("Role manager is null");
					}
					log.info("Has role permissions: "+permissions);
					acnt = new SimpleAccount(username, "", getName() );
					for(String perm: permissions){
						acnt.addStringPermission(perm);
					}
					tokenMap.put(username, acnt);

				} catch (ParseException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

        	}
            
             
        } else {
        	
        	//System user should be approved by the system realm
        	if("system".equals(upToken.getUsername()) ){
        		return null;
        	}
        	
        	
        	String userName = upToken.getUsername();
        	//todo check usenamr and pw against user repository
        	String loginTopic = "/topic/"+GossCoreContants.PROP_TOKEN_QUEUE;
        	acnt = new SimpleAccount(upToken.getUsername(), upToken.getPassword(), getName() );
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Connection:create");
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Queue:create");
        	acnt.addStringPermission("topic:ActiveMQ.Advisory.Consumer.Queue.temp.token_resp."+userName);
        	acnt.addStringPermission("topic:"+GossCoreContants.PROP_TOKEN_QUEUE+":write");
        	acnt.addStringPermission("topic:"+GossCoreContants.PROP_TOKEN_QUEUE+":create");
        	acnt.addStringPermission("queue:temp.token_resp."+userName);


        	tokenMap.put(username, acnt);
        }
		return acnt;
	}
	

	@Override
	public Set<String> getPermissions(String identifier) {
		//I don't believe this is used
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
