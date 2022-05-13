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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlatformGateway {
    public static final String URL_PATH_EVENTS = "/events";
    public static final String URL_PATH_ALARMS = "/alarms";
    public static final String URL_PATH_ALARMS_LIST = URL_PATH_ALARMS + "/list";
    public static final String URL_PATH_ALARMS_ACK = URL_PATH_ALARMS + "/{%d}/ack";
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
            log.error("Error happened when post {} at {} on platform", data, path, e);
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
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            log.error("Error happened when execute get request at {} on platform", path, e);
            return null;
        }
    }

    public boolean put(String path, String authToken, JsonNode data) {
        try {
            HttpPut putRequest = new HttpPut(platformUrl + path);
            putRequest.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            putRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            putRequest.addHeader(HttpHeaders.AUTHORIZATION, authToken);
            putRequest.setEntity(new StringEntity(data.toString()));
            try(CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(putRequest)) {
                log.info("Put request to platform {} with data {} return code {}", path, data, response.getStatusLine().getStatusCode());
                return true;
            }
        } catch (IOException e) {
            log.error("Error happened when put {} at {}", data, path);
            return false;
        }
    }

    public boolean delete(String path, String authToken) {
        try {
            HttpDelete deleteRequest = new HttpDelete(platformUrl + path);
            deleteRequest.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            deleteRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            deleteRequest.addHeader(HttpHeaders.AUTHORIZATION, authToken);
            try(CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(deleteRequest)) {
                log.info("un-ack alarm with url path {}", path);
                return true;
            }

        } catch (IOException e) {
            return false;
        }
    }
}
