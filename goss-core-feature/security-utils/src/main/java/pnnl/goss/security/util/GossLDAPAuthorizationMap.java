package pnnl.goss.security.util;

import java.io.FileWriter;
import java.util.Set;

import javax.naming.directory.DirContext;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.LDAPAuthorizationMap;

public class GossLDAPAuthorizationMap extends LDAPAuthorizationMap {


	@Override
	public String getAuthentication() {
		String returnString = "";
		try{
			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getAuthentication.log",true);
			logWriter.write(System.nanoTime()+";");
			returnString = super.getAuthentication();
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return returnString;
	}

	@Override
	public Set<GroupPrincipal> getAdminACLs(ActiveMQDestination destination) {
		Set<GroupPrincipal> acls = null;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getAdminACLs.log",true);
			logWriter.write(System.nanoTime()+";");
			acls = super.getAdminACLs(destination);
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return acls;
	}

	@Override
	public DirContext getContext() {
		return super.getContext();
	}

	@Override
	public Set<GroupPrincipal> getReadACLs(ActiveMQDestination destination) {
		Set<GroupPrincipal> groupPrincipals = null;
		try{
			FileWriter logWriter = new FileWriter("GossLDAPAuthorizationMap_getReadACLs.log",true);
			logWriter.write(System.nanoTime()+";");
			groupPrincipals = super.getReadACLs(destination);
			logWriter.write(System.nanoTime()+"\n");
			logWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return groupPrincipals;
	}

	@Override
	public String getReadAttribute() {
		return super.getReadAttribute();
	}

	@Override
	public boolean isQueueSearchSubtreeBool() {
		return super.isQueueSearchSubtreeBool();
	}

	@Override
	public boolean isTopicSearchSubtreeBool() {
		return super.isTopicSearchSubtreeBool();
	}

	@Override
	public Set<GroupPrincipal> getWriteACLs(ActiveMQDestination destination) {
		return super.getWriteACLs(destination);
	}
}
