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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.PermissionAdapter;

@Component(service = PermissionAdapter.class)
public class SecurityManagerRealmHandler implements PermissionAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityManagerRealmHandler.class);

    private final Map<ServiceReference<GossRealm>, GossRealm> realmMap = new ConcurrentHashMap<>();

    @Activate
    public void activate() {
        log.info("SecurityManagerRealmHandler activated with {} known realms", realmMap.size());
    }

    /**
     * Tracks GossRealm services so this component can answer getPermissions()
     * across every realm (its PermissionAdapter role).
     *
     * This binder deliberately does NOT call setRealms() on the SecurityManager.
     * The SecurityManager component (Activator) is the single writer of the
     * SecurityManager's realm set; it collects the same GossRealm services and owns
     * setRealms(). Having two independently-populated maps both write the shared
     * SecurityManager was the double-writer race in GADP-012 / issue #1882: a
     * partial write here could transiently drop the system realm from the set
     * GridOpticsServer authenticates against. Realm wiring is now owned solely by
     * the Activator; this component only reads realms for permissions.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "realmRemoved")
    public void realmAdded(ServiceReference<GossRealm> ref, GossRealm handler) {
        realmMap.put(ref, handler);
        log.debug("Realm tracked for permissions: {}", handler.getClass().getName());
    }

    public void realmRemoved(ServiceReference<GossRealm> ref) {
        GossRealm removed = realmMap.remove(ref);
        if (removed != null) {
            log.debug("Realm untracked for permissions: {}", removed.getClass().getName());
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
