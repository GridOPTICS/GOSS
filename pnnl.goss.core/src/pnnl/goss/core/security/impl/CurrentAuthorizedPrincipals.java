//package pnnl.goss.core.security.impl;
//
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.apache.felix.dm.annotation.api.Component;
//import org.apache.felix.dm.annotation.api.ServiceDependency;
//import org.apache.shiro.authc.Account;
//import org.apache.shiro.authc.AuthenticationException;
//import org.apache.shiro.authc.AuthenticationInfo;
//import org.apache.shiro.authc.AuthenticationListener;
//import org.apache.shiro.authc.AuthenticationToken;
//import org.apache.shiro.authc.SimpleAccount;
//import org.apache.shiro.mgt.SecurityManager;
//import org.apache.shiro.subject.PrincipalCollection;
//
//import pnnl.goss.core.security.PermissionAdapter;
//
//@Component
//public class CurrentAuthorizedPrincipals implements AuthenticationListener, PermissionAdapter {
//	
//	private final Map<String, SimpleAccount> accountMap = new ConcurrentHashMap<>();
//	private final Set<PrincipalCollection> successful = new HashSet<>();
//	
//	@ServiceDependency
//	private volatile SecurityManager securityManager;
//	
//	@Override
//	public void onSuccess(AuthenticationToken token,
//			AuthenticationInfo info) {
//		
//		successful.add(info.getPrincipals());
////		
////		for (Realm realm : ((RealmSecurityManager) securityManager).getRealms()){
////		    System.out.println(realm.getName());
////		}
////		// TODO Assumes AuthenticationInfo also implements Account interface!
////		accountMap.put(token.getPrincipal().toString(), (SimpleAccount)info);
//		
//		
//		System.out.println("OnSuccess!" + info + " Is SimpleAccount? "+(info instanceof SimpleAccount));
//
//	}
//
//	@Override
//	public void onLogout(PrincipalCollection principals) {
//		// TODO Auto-generated method stub
//		for(Object principal : principals.asList()){
//			accountMap.remove(principal.toString());
//		}
//		System.out.println("OnLogout " + principals);
//	}
//
//	@Override
//	public void onFailure(AuthenticationToken token,
//			AuthenticationException ae) {
//		System.out.println("OnFailure: " + token
//				+ " exception: " + ae);
//
//	}
//
//	@Override
//	public Set<String> getPermissions(String identifier) {
//		
//		if (!accountMap.containsKey(identifier)){
//			throw new AuthenticationException();
//		}
//		
//		Account account = accountMap.get(identifier);
//		
//		return new HashSet<String>(account.getStringPermissions());
//		
//	}
//}
