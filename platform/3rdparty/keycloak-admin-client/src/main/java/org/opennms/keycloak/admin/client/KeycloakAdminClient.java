package org.opennms.keycloak.admin.client;

import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public interface KeycloakAdminClient {
    KeycloakAdminClientSession login(String realm, String user, String password) throws IOException, URISyntaxException, AuthenticationException, KeycloakAuthenticationException;
}
