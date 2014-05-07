package pnnl.goss.security.util;

import java.util.Set;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.CachedLDAPAuthorizationMap;

public class GossLDAPAuthorizationMap extends CachedLDAPAuthorizationMap {
	protected final String topicStr = "topic://";
	protected String advisoryBase = "ActiveMQ.Advisory";
	protected boolean useAdvisorySearchBase = true;

	@Override
	public Set<Object> getAdminACLs(ActiveMQDestination destination) {
		if (AdvisorySupport.isAdvisoryTopic(destination) && useAdvisorySearchBase) {
			destination = destination.createDestination(topicStr+advisoryBase);
        } 
		return super.getAdminACLs(destination);
	}
	@Override
	public Set<Object> getReadACLs(ActiveMQDestination destination) {
		if (AdvisorySupport.isAdvisoryTopic(destination) && useAdvisorySearchBase) {
			destination = destination.createDestination(topicStr+advisoryBase);
        } 
		return super.getReadACLs(destination);
	}
	@Override
	public Set<Object> getWriteACLs(ActiveMQDestination destination) {
		if (AdvisorySupport.isAdvisoryTopic(destination) && useAdvisorySearchBase) {
			destination = destination.createDestination(topicStr+advisoryBase);
        } 
		return super.getWriteACLs(destination);
	}

	public String getAdvisoryBase() {
		return advisoryBase;
	}

	public void setAdvisoryBase(String advisoryBase) {
		this.advisoryBase = advisoryBase;
	}

	public boolean isUseAdvisorySearchBase() {
		return useAdvisorySearchBase;
	}

	public void setUseAdvisorySearchBase(boolean useAdvisorySearchBase) {
		this.useAdvisorySearchBase = useAdvisorySearchBase;
	}
	
	
	
//
//	@Override
//	public Set<GroupPrincipal> getAdminACLs(ActiveMQDestination destination) {
//		Set<GroupPrincipal> acls = null;
//		try{
//			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getAdminACLs.log",true);
//			logWriter.write(System.nanoTime()+";");
//			acls = getAdminACLs(destination);
//			logWriter.write(System.nanoTime()+"\n");
//			logWriter.close();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		return acls;
//	}
//
//	@Override
//	public DirContext getContext() {
//		return super.getContext();
//	}
//
//	@Override
//	public Set<GroupPrincipal> getReadACLs(ActiveMQDestination destination) {
//		Set<GroupPrincipal> groupPrincipals = null;
//		try{
//			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getReadACLs.log",true);
//			logWriter.write(System.nanoTime()+";");
//			groupPrincipals = super.getReadACLs(destination);
//			logWriter.write(System.nanoTime()+"\n");
//			logWriter.close();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		return groupPrincipals;
//	}
//
//	@Override
//	public String getReadAttribute() {
//		return super.getReadAttribute();
//	}
//
//	@Override
//	public boolean isQueueSearchSubtreeBool() {
//		return super.isQueueSearchSubtreeBool();
//	}
//
//	@Override
//	public boolean isTopicSearchSubtreeBool() {
//		return super.isTopicSearchSubtreeBool();
//	}
//
//	@Override
//	public Set<GroupPrincipal> getWriteACLs(ActiveMQDestination destination) {
//		return super.getWriteACLs(destination);
//	}
	
//	public String getAuthentication() {
//		String returnString = "";
//		try{
//			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getAuthentication.log",true);
//			logWriter.write(System.nanoTime()+";");
//			returnString = super.getAuthentication();
//			logWriter.write(System.nanoTime()+"\n");
//			logWriter.close();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return returnString;
//	}
	
//	@Override
//	protected void query() throws Exception {
//	
//		System.out.println("DO QUERY");
//		// TODO Auto-generated method stub
//		super.query();
//	}
	
//	@Override
//	protected void processQueryResults(DefaultAuthorizationMap map,
//			NamingEnumeration<SearchResult> results,
//			DestinationType destinationType, PermissionType permissionType)
//			throws Exception {
//		System.out.println("PROCESS QUERY RESULTS");
//		// TODO Auto-generated method stub
//		super.processQueryResults(map, results, destinationType, permissionType);
//	}
	
//	@Override
//	protected AuthorizationEntry getEntry(DefaultAuthorizationMap map,
//			LdapName dn, DestinationType destinationType) {
////		System.out.println("GET ENTRY "+dn);
//		return super.getEntry(map, dn, destinationType);
//	}
//	@Override
//	protected ActiveMQDestination formatDestination(LdapName dn,
//			DestinationType destinationType) {
////		System.out.println("FORMAT DESTINATION "+dn);
//		return super.formatDestination(dn, destinationType);
//	}
}
