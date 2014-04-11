package pnnl.goss.security.util;

import java.util.Date;
import java.util.Set;

import javax.naming.directory.DirContext;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.LDAPAuthorizationMap;

public class GossLDAPAuthorizationMap extends LDAPAuthorizationMap {

	
	@Override
	public String getAuthentication() {
		System.out.println("AUTH MAP: GET AUTHENTICATION");
		// TODO Auto-generated method stub
		return super.getAuthentication();
	}
	
	@Override
	public Set<GroupPrincipal> getAdminACLs(ActiveMQDestination destination) {
		// TODO Auto-generated method stub
		long start = new Date().getTime();
		
		System.out.println("AUTH MAP: GET ADMIN ACLS");
		Set<GroupPrincipal> acls = super.getAdminACLs(destination);
		long end = new Date().getTime();
		long diff = end-start;
		System.out.println("TIME "+diff);
		System.out.println("ACLS "+acls);
		return acls;
	}
	
@Override
	public DirContext getContext() {
		// TODO Auto-generated method stub
	System.out.println("AUTH MAP: GET CONTEXT");
		return super.getContext();
	}
	
	@Override
	public Set<GroupPrincipal> getReadACLs(ActiveMQDestination destination) {
		// TODO Auto-generated method stub
		System.out.println("AUTH MAP: GET READ ACLS");
		return super.getReadACLs(destination);
	}
	
@Override
	public String getReadAttribute() {
		// TODO Auto-generated method stub
	System.out.println("AUTH MAP: GET READ ATTR");
		return super.getReadAttribute();
	}

@Override
public boolean isQueueSearchSubtreeBool() {
	// TODO Auto-generated method stub
	System.out.println("AUTH MAP: IS QUEUE SEARCH SUBTREE");
	return super.isQueueSearchSubtreeBool();
}

@Override
public boolean isTopicSearchSubtreeBool() {
	// TODO Auto-generated method stub
	System.out.println("AUTH MAP: IS TOPIC SEARCH SUBTREE");
	return super.isTopicSearchSubtreeBool();
}

@Override
public Set<GroupPrincipal> getWriteACLs(ActiveMQDestination destination) {
	// TODO Auto-generated method stub
	System.out.println("AUTH MAP: GET WRITE ACLS");
	return super.getWriteACLs(destination);
}
}
