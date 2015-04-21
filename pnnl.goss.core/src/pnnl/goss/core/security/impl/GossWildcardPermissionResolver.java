package pnnl.goss.core.security.impl;

import org.apache.activemq.shiro.authz.ActiveMQWildcardPermission;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;

public class GossWildcardPermissionResolver extends WildcardPermissionResolver {

	//Returns case sensitive permissions (before it was converting them to lower case)
	
	/**
	 * Returns a new {@link WildcardPermission WildcardPermission} instance constructed based on the specified
	 * <tt>permissionString</tt>.
	 *
	 * @param permissionString the permission string to convert to a {@link Permission Permission} instance.
	 * @return a new {@link WildcardPermission WildcardPermission} instance constructed based on the specified
	 *         <tt>permissionString</tt>
	 */
	@Override
	public Permission resolvePermission(String permissionString) {
		if(permissionString!=null && (permissionString.startsWith("topic:") || permissionString.startsWith("queue:") 
				 || permissionString.startsWith("temp-queue:"))){
			return new ActiveMQWildcardPermission(permissionString);
		} else 
		{
			return new WildcardPermission(permissionString, true);
		}
	}
}
