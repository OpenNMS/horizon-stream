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

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlatformGateway {
    public static final String URL_PATH_EVENTS = "/events";
    public static final String URL_PATH_ALARMS = "/alarms";
    private ObjectMapper jsonMapper = new ObjectMapper();
    @Value("${horizon-stream.core.url}")
    private String platformUrl;

    public boolean post(String path, String authToken, String data) {
        try {
            HttpPost post = new HttpPost(platformUrl + path);
            post.setEntity(new StringEntity(data));
            post.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            post.addHeader(HttpHeaders.AUTHORIZATION, authToken);
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {
                return response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED;
            }
        }catch (Exception e) {
            log.error("Error happened when posting {} at {} on platform", data, path, e);
            return false;
        }
    }

    public String get(String path, String authToken) {
        try {
            HttpGet getRequest = new HttpGet(platformUrl + path);
            getRequest.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            getRequest.setHeader(HttpHeaders.AUTHORIZATION, authToken);
            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(getRequest)) {
                if(response.getStatusLine().getStatusCode()!=200) {
                    log.info("Response from platform with status {} and content {}", response.getStatusLine().getStatusCode(), response);
                    return "{}";
                }
                return response.getEntity().toString();
            }
        } catch (IOException e) {
            log.error("Error happened when execute get request at {} on platform", path, e);
            return null;
        }
    }

    private JsonNode strToJsonNode(String data) throws JsonProcessingException {
        return jsonMapper.readTree(data);
    }
}
