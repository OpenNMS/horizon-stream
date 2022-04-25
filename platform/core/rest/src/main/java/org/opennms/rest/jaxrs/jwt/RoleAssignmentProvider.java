package org.opennms.rest.jaxrs.jwt;

import java.util.List;

public interface RoleAssignmentProvider {
    List<String> lookupUserRoles(String username);
}
