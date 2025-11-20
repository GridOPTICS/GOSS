package pnnl.goss.core.security.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.osgi.framework.ServiceReference;

import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.security.PermissionAdapter;

@Component(service = PermissionAdapter.class)
public class SecurityManagerRealmHandler implements PermissionAdapter {

    @Reference
    private volatile SecurityManager securityManager;
    private final Map<ServiceReference<GossRealm>, GossRealm> realmMap = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "realmRemoved")
    public void realmAdded(ServiceReference<GossRealm> ref, GossRealm handler) {

        DefaultSecurityManager defaultInstance = (DefaultSecurityManager) securityManager;
        realmMap.put(ref, handler);

        if (defaultInstance.getRealms() == null) {
            defaultInstance.setRealms(new HashSet<Realm>());
            Set<Realm> realms = new HashSet<>();
            for (GossRealm r : realmMap.values()) {
                realms.add((Realm) r);
            }
            defaultInstance.setRealms(realms);
        } else {
            defaultInstance.getRealms().add(handler);
        }

    }

    public void realmRemoved(ServiceReference<GossRealm> ref) {
        DefaultSecurityManager defaultInstance = (DefaultSecurityManager) securityManager;
        defaultInstance.getRealms().remove(realmMap.get(ref));
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
