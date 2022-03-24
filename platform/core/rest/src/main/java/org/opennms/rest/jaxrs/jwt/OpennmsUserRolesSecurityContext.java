package org.opennms.rest.jaxrs.jwt;

import org.apache.http.auth.BasicUserPrincipal;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class OpennmsUserRolesSecurityContext implements SecurityContext {

    private final Principal userPrincipal;
    private final Set<String> roles;

    public OpennmsUserRolesSecurityContext(String username, List<String> roles) {
        this.userPrincipal = new BasicUserPrincipal(username);
        this.roles = new TreeSet<>(roles);
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
