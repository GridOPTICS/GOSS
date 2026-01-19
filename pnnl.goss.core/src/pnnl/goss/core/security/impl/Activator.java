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
 * SecurityManager before exposing the SecurityManager service. This ensures
 * that authentication can work immediately when the broker starts.
 *
 * IMPORTANT: This component requires at least one GossRealm to be available
 * (cardinality = AT_LEAST_ONE) before the SecurityManager service is
 * registered. This prevents the race condition where GridOpticsServer tries to
 * authenticate before any realms are configured.
 */
@Component(service = SecurityManager.class, immediate = true)
public class Activator extends DefaultActiveMqSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    private final Map<ServiceReference<GossRealm>, GossRealm> realmMap = new ConcurrentHashMap<>();

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
