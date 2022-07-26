/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.server.service;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletWebRequest;

import graphql.GraphQLContext;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.autoconfigure.DefaultGlobalContext;
import io.leangen.graphql.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlatformGateway {
    public static final String URL_PATH_EVENTS = "/events";
    public static final String URL_PATH_ALARMS = "/alarms";
    public static final String URL_PATH_DEVICES = "/devices";
    public static final String URL_PATH_ALARMS_LIST = URL_PATH_ALARMS + "/list";
    public static final String URL_PATH_ALARMS_ACK = URL_PATH_ALARMS + "/%d/ack";
    public static final String URL_PATH_ALARMS_CLEAR = URL_PATH_ALARMS + "/%d/clear";
    public static final String URL_PATH_MINIONS = "/minions";
    public static final String URL_PATH_MINIONS_ID = "/minions/%s";
    private final String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public PlatformGateway(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthHeader(ResolutionEnvironment env) {
        GraphQLContext graphQLContext = env.dataFetchingEnvironment.getContext();
        DefaultGlobalContext context = (DefaultGlobalContext) ContextUtils.unwrapContext(graphQLContext);
        ServletWebRequest request = (ServletWebRequest) context.getNativeRequest();
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    public <T> ResponseEntity<T> post(String path, String authToken, Object data, Class<T> returnType) {
        HttpHeaders headers = createHeaders(authToken, true);
        HttpEntity request = new HttpEntity(data, headers);
        return executeRequest(path, HttpMethod.POST, request, returnType);
    }

    public <T> ResponseEntity<T> get(String path, String authToken, Class<T> returnType) {
            HttpHeaders headers = createHeaders(authToken, false);
            HttpEntity request = new HttpEntity(headers);
            return executeRequest(path, HttpMethod.GET, request, returnType);
    }

    public <T> ResponseEntity<T> put(String path, String authToken, Object data, Class<T> returnType) {
        HttpHeaders headers = createHeaders(authToken, true);
        HttpEntity request = new HttpEntity(data, headers);
        return executeRequest(path, HttpMethod.PUT, request, returnType);
    }

    public ResponseEntity<String> delete(String path, String authToken) {
        return executeRequest(path, HttpMethod.DELETE, new HttpEntity(createHeaders(authToken, false)), String.class);
    }

    private HttpHeaders createHeaders(String authToken, boolean hasRequestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, authToken);
        if(hasRequestBody) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }

    private <T> ResponseEntity<T> executeRequest(String path, HttpMethod method, HttpEntity request, Class<T> type) {
        ResponseEntity<T> response = restTemplate.exchange(baseUrl + path, method, request, type);
        log.info("Response from platform with code {}, {}", response.getStatusCode(), response.hasBody());
        return response;
    }
}
