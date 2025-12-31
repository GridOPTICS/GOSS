package pnnl.goss.core.security.impl;

import org.apache.activemq.shiro.mgt.DefaultActiveMqSecurityManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi DS component that provides the Shiro SecurityManager service.
 *
 * This replaces the old Felix DM Activator. The SecurityManager is used by GOSS
 * for authentication and authorization.
 */
@Component(service = SecurityManager.class, immediate = true)
public class Activator extends DefaultActiveMqSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    @Activate
    public void activate() {
        log.info("Activating SecurityManager service");

        // Configure cache manager for authorization caching
        // This eliminates the "No authorizationCache instance set" warnings
        // and improves performance by caching authorization lookups
        setCacheManager(new MemoryConstrainedCacheManager());
        log.debug("CacheManager configured for authorization caching");

        SecurityUtils.setSecurityManager(this);
        log.info("SecurityManager registered with SecurityUtils");
    }

    @Deactivate
    public void deactivate() {
        log.info("Deactivating SecurityManager service");
    }
}
