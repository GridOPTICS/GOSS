package pnnl.goss.core.security.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.PermissionAdapter;

@Component(service = PermissionAdapter.class)
public class SecurityManagerRealmHandler implements PermissionAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityManagerRealmHandler.class);

    @Reference
    private volatile SecurityManager securityManager;
    private final Map<ServiceReference<GossRealm>, GossRealm> realmMap = new ConcurrentHashMap<>();

    @Activate
    public void activate() {
        log.info("SecurityManagerRealmHandler activated with {} pending realms", realmMap.size());
        // Register any realms that were added before the SecurityManager was available
        if (!realmMap.isEmpty()) {
            registerAllRealms();
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "realmRemoved")
    public void realmAdded(ServiceReference<GossRealm> ref, GossRealm handler) {
        realmMap.put(ref, handler);
        log.debug("Realm added: {}", handler.getClass().getName());

        // Only register if the SecurityManager is available
        if (securityManager != null) {
            registerAllRealms();
        }
    }

    private synchronized void registerAllRealms() {
        if (securityManager == null) {
            log.warn("Cannot register realms - SecurityManager is null");
            return;
        }

        DefaultSecurityManager defaultInstance = (DefaultSecurityManager) securityManager;
        Set<Realm> realms = new HashSet<>();
        for (GossRealm r : realmMap.values()) {
            realms.add((Realm) r);
        }
        defaultInstance.setRealms(realms);
        log.info("Registered {} realms with SecurityManager", realms.size());
    }

    public void realmRemoved(ServiceReference<GossRealm> ref) {
        GossRealm removed = realmMap.remove(ref);
        if (removed != null && securityManager != null) {
            DefaultSecurityManager defaultInstance = (DefaultSecurityManager) securityManager;
            if (defaultInstance.getRealms() != null) {
                defaultInstance.getRealms().remove(removed);
            }
        }
    }

    @Override
    public Set<String> getPermissions(String identifier) {

        Set<String> perms = new HashSet<>();
        for (GossRealm r : realmMap.values()) {
            perms.addAll(r.getPermissions(identifier));
        }

        return perms;
    }

}
