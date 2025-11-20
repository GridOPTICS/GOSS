package pnnl.goss.core.security;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import pnnl.goss.core.Request;

@Component(service = AuthorizationHandler.class)
public class AuthorizeAll implements AuthorizationHandler {

    @Override
    public boolean isAuthorized(Request request, Set<String> permissions) {
        return true;
    }
}
