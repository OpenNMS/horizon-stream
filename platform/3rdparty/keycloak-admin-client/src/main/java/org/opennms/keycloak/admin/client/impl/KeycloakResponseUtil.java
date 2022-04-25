package org.opennms.keycloak.admin.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;

import java.io.IOException;
import java.io.InputStream;

public class KeycloakResponseUtil {

    public static final KeycloakResponseUtil INSTANCE = new KeycloakResponseUtil();

    private ObjectMapper objectMapper = new ObjectMapper();

//========================================
// Getters and Setters
//----------------------------------------

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

//========================================
// Operations
//----------------------------------------

    public AccessTokenResponse parseAccessTokenResponse(HttpResponse response) throws KeycloakAuthenticationException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                AccessTokenResponse result = objectMapper.readValue(inputStream, AccessTokenResponse.class);

                return result;
            } catch (IOException ioException) {
                throw new KeycloakAuthenticationException("failed to parse response body", ioException);
            }
        } else {
            throw new KeycloakAuthenticationException("login request status " + statusCode);
        }
    }

    public UserRepresentation parseUserResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, UserRepresentation.class, "retrieve user");
    }

    public UserRepresentation[] parseUserArrayResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, UserRepresentation[].class, "retrieve user(s)");
    }

    public MappingsRepresentation parseMappingResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, MappingsRepresentation.class, "retrieve user role mappings");
    }

    public RoleRepresentation[] parseRoleArrayResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, RoleRepresentation[].class, "retrieve role by name");
    }

//========================================
// Internals
//----------------------------------------

    private <T> T parseResponseCommon(HttpResponse response, Class<T> clazz, String operationDescription) throws KeycloakAuthenticationException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                T result = objectMapper.readValue(inputStream, clazz);

                return result;
            } catch (IOException ioException) {
                throw new KeycloakAuthenticationException("failed to parse response body", ioException);
            }
        } else {
            throw new KeycloakAuthenticationException(operationDescription + " status " + statusCode);
        }
    }
}
