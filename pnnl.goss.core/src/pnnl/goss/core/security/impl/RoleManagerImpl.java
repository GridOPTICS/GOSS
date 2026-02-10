package pnnl.goss.core.security.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.security.RoleManager;

@Component(service = RoleManager.class, configurationPid = "pnnl.goss.core.security.rolefile", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class RoleManagerImpl implements RoleManager {

    private static final Logger log = LoggerFactory.getLogger(RoleManagerImpl.class);

    private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();

    @Activate
    public void activate(Map<String, Object> properties) {
        log.info("Activating RoleManagerImpl");
        updated(properties);
    }

    @Modified
    public synchronized void updated(Map<String, Object> properties) {
        if (properties != null) {
            log.debug("Updating RoleManagerImpl");
            rolePermissions.clear();

            for (String k : properties.keySet()) {
                // Skip OSGi/ConfigAdmin metadata properties
                if (k.startsWith("service.") || k.startsWith("component.") ||
                        k.startsWith("felix.") || k.equals("osgi.ds.satisfying.condition.target")) {
                    continue;
                }

                Object value = properties.get(k);
                if (!(value instanceof String)) {
                    continue;
                }

                String v = (String) value;
                String[] credAndPermissions = v.split(",");
                Set<String> perms = new HashSet<>();

                for (int i = 0; i < credAndPermissions.length; i++) {
                    perms.add(credAndPermissions[i]);
                }
                rolePermissions.put(k, perms);
            }
            log.info("RoleManagerImpl configured with {} roles", rolePermissions.size());
        }
    }

    @Override
    public Set<String> getRolePermissions(String roleName) throws Exception {
        if (rolePermissions.containsKey(roleName)) {
            return rolePermissions.get(roleName);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getAllRoles() {
        return rolePermissions.keySet();
    }

    @Override
    public Set<String> getRolePermissions(List<String> roleNames) throws Exception {
        Set<String> perms = new HashSet<>();
        for (String role : roleNames) {
            Set<String> rolePerms = rolePermissions.get(role);
            if (rolePerms != null) {
                for (String p : rolePerms) {
                    if (!perms.contains(p)) {
                        perms.add(p);
                    }
                }
            }
        }

        return perms;
    }

}
