package pnnl.goss.core.security.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.shiro.mgt.DefaultActiveMqSecurityManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.GossRealm;

/**
 * OSGi DS component that provides the Shiro SecurityManager service.
 *
 * This replaces the old Felix DM Activator. The SecurityManager is used by GOSS
 * for authentication and authorization.
 *
 * This component collects all GossRealm services and registers them with the
 * SecurityManager before exposing the SecurityManager service. It is the SINGLE
 * writer of the SecurityManager's realm set (SecurityManagerRealmHandler no
 * longer writes it), so the realm set cannot be clobbered mid-startup.
 *
 * IMPORTANT: The SecurityManager service is not published until BOTH the
 * AT_LEAST_ONE realmAdded binder has a realm AND the mandatory, target-filtered
 * systemRealm reference is bound. The AT_LEAST_ONE guard alone was insufficient
 * (GADP-012 / issue #1882): it guaranteed some realm, not the
 * system-authenticating one, so GridOpticsServer could connect as
 * system/manager against a realm set that returned null for "system". The
 * systemRealm reference closes that gap.
 */
@Component(service = SecurityManager.class, immediate = true)
public class Activator extends DefaultActiveMqSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    private final Map<ServiceReference<GossRealm>, GossRealm> realmMap = new ConcurrentHashMap<>();

    /**
     * Mandatory, target-filtered dependency on the system-authenticating realm
     * (SystemBasedRealm, marked with the realm.type=system service property).
     *
     * This reference is the ordering guarantee for GADP-012 / issue #1882.
     * Declarative Services will not invoke this component's @Activate, and thus
     * will not publish the SecurityManager service, until the realm that
     * authenticates the system/manager principal with the "*" permission is bound.
     * Because GridOpticsServer holds a mandatory reference on the SecurityManager
     * service, gating the service publication here transitively gates
     * GridOpticsServer's broker-connect (createConnection("system", "manager")) on
     * the system realm being present.
     *
     * Mandatory cardinality (the DS default 1..1) is what forces the ordering: DS
     * holds activation until the reference is bound. Binding does not deadlock,
     * because SystemBasedRealm depends only on SecurityConfig and
     * GossPermissionResolver, neither of which depends back on the SecurityManager.
     * The same realm instance is also collected by the dynamic realmAdded binder
     * below, so it is included in the realm set.
     *
     * Never read directly in Java: the field's sole purpose is to give DS a
     * mandatory reference to gate @Activate on, so it is unused from this class's
     * own code and the compiler's unused-field warning is suppressed.
     */
    @Reference(target = GossRealm.SYSTEM_REALM_TARGET_FILTER)
    @SuppressWarnings("unused")
    private volatile GossRealm systemRealm;

    @Activate
    public void activate() {
        log.info("Activating SecurityManager service");

        // Configure cache manager for authorization caching
        // This eliminates the "No authorizationCache instance set" warnings
        // and improves performance by caching authorization lookups
        setCacheManager(new MemoryConstrainedCacheManager());
        log.debug("CacheManager configured for authorization caching");

        // Register any realms that were added before activation
        if (!realmMap.isEmpty()) {
            registerAllRealms();
        }

        SecurityUtils.setSecurityManager(this);
        log.info("SecurityManager registered with SecurityUtils");
    }

    @Deactivate
    public void deactivate() {
        log.info("Deactivating SecurityManager service");
    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, unbind = "realmRemoved")
    public void realmAdded(ServiceReference<GossRealm> ref, GossRealm handler) {
        realmMap.put(ref, handler);
        log.info("Realm added to SecurityManager: {}", handler.getClass().getName());
        registerAllRealms();
    }

    public void realmRemoved(ServiceReference<GossRealm> ref) {
        GossRealm removed = realmMap.remove(ref);
        if (removed != null) {
            log.info("Realm removed from SecurityManager: {}", removed.getClass().getName());
            registerAllRealms();
        }
    }

    private synchronized void registerAllRealms() {
        Set<Realm> realms = new HashSet<>();
        for (GossRealm r : realmMap.values()) {
            realms.add((Realm) r);
        }
        setRealms(realms);
        log.info("Registered {} realms with SecurityManager: {}", realms.size(),
                realms.stream().map(r -> r.getClass().getSimpleName()).toList());
    }
}
