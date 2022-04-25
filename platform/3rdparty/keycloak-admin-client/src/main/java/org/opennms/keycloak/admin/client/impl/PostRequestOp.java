package org.opennms.keycloak.admin.client.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface PostRequestOp {
    boolean process(HttpUriRequest request, HttpResponse response);
}
