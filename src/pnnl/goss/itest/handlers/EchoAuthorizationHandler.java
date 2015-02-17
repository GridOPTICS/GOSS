package pnnl.goss.itest.handlers;

import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.Request;
import pnnl.goss.security.core.authorization.AbstractAccessControlHandler;

public class EchoAuthorizationHandler extends AbstractAccessControlHandler {

    List<String> roles = null;

    @Override
    public List<String> getAllowedRoles(Request request) {

        if (roles == null){
            roles = new ArrayList<>();
            roles.add("fine-grain-users");
        }

        return roles;
    }

}
