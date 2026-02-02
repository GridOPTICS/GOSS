package pnnl.goss.core.security;

import java.util.Set;

import org.apache.shiro.realm.Realm;

/**
 * GossRealm combines Shiro's Realm with PermissionAdapter functionality. This
 * allows classes implementing GossRealm to satisfy both dependencies: -
 * SecurityManager needs Realm instances - HandlerRegistryImpl needs
 * PermissionAdapter
 */
public interface GossRealm extends Realm, PermissionAdapter {

    Set<String> getPermissions(String identifier);

    boolean hasIdentifier(String identifier);

}
