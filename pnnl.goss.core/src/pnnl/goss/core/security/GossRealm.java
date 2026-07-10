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

    /**
     * Service property name used to mark the system-authenticating realm
     * (SystemBasedRealm) so ordering-sensitive consumers can select it with a
     * target filter rather than accepting any bound GossRealm. See
     * {@link #SYSTEM_REALM_TYPE} and GADP-012 / issue #1882.
     */
    String REALM_TYPE_PROPERTY = "realm.type";

    /**
     * Service property value published by the system-authenticating realm
     * (SystemBasedRealm). Consumers gate activation on this realm with
     * {@code @Reference(target = SYSTEM_REALM_TARGET_FILTER)}.
     */
    String SYSTEM_REALM_TYPE = "system";

    /**
     * The literal target filter string consumers pass to
     * {@code @Reference(target = ...)} to select the system realm. Kept as its own
     * constant (rather than assembled from the two constants above at runtime)
     * because {@code @Reference(target = ...)} requires a compile-time constant
     * expression, and bnd is strict about resolving that expression to a literal
     * when it emits the OSGI-INF descriptor.
     */
    String SYSTEM_REALM_TARGET_FILTER = "(" + REALM_TYPE_PROPERTY + "=" + SYSTEM_REALM_TYPE + ")";

    Set<String> getPermissions(String identifier);

    boolean hasIdentifier(String identifier);

}
