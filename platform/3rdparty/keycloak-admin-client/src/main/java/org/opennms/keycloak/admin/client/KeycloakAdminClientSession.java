package org.opennms.keycloak.admin.client;

import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.keycloak.admin.client.exc.KeycloakBaseException;
import org.opennms.keycloak.admin.client.exc.KeycloakOperationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public interface KeycloakAdminClientSession {
    String getInitialAccessToken();
    String getInitialRefreshToken();

    UserRepresentation getUserByUsername(String realm, String username) throws IOException, URISyntaxException, KeycloakBaseException;
    UserRepresentation getUserById(String realm, String userId) throws IOException, URISyntaxException, KeycloakBaseException;
    MappingsRepresentation getUserRoleMappings(String realm, String userId) throws IOException, URISyntaxException, KeycloakBaseException;
    void addRealm(String realm, Consumer<RealmRepresentation> realmCustomizer) throws IOException, URISyntaxException, KeycloakBaseException;
    void addUser(String realm, String username, Consumer<UserRepresentation> userCustomizer) throws IOException, URISyntaxException, KeycloakBaseException;

    RoleRepresentation getRoleByName(String realm, String roleName) throws IOException, URISyntaxException, KeycloakBaseException;
    void createRole(String realm, String roleName) throws IOException, URISyntaxException, KeycloakBaseException;

    /**
     * NOTE: both realm name and realm ID are required by Keycloak.
     *
     * @param realm
     * @param userId
     * @param roleName
     * @param roleId
     * @throws IOException
     * @throws URISyntaxException
     * @throws KeycloakBaseException
     */
    void assignUserRole(String realm, String userId, String roleName, String roleId) throws IOException, URISyntaxException, KeycloakBaseException;

    void logout() throws IOException, URISyntaxException, KeycloakOperationException;
}
