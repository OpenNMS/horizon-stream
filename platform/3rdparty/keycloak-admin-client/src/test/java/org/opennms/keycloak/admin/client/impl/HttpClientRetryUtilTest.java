package org.opennms.keycloak.admin.client.impl;

import junit.framework.TestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpClientRetryUtilTest extends TestCase {
    private HttpClientRetryUtil target;

    private HttpClient mockHttpClient;
    private PostRequestOp mockPostRequestOp;
    private HttpResponse mockResponse;

    private HttpUriRequest testRequest;

    @Override
    public void setUp() throws Exception {
        target = new HttpClientRetryUtil();

        mockHttpClient = Mockito.mock(HttpClient.class);
        mockPostRequestOp = Mockito.mock(PostRequestOp.class);
        mockResponse = Mockito.mock(HttpResponse.class);

        testRequest = new HttpGet();

        Mockito.when(mockHttpClient.execute(testRequest)).thenReturn(mockResponse);
    }

    @Test
    public void testExecuteWithNoRetry() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockPostRequestOp.process(testRequest, mockResponse)).thenReturn(false);

        //
        // Execute
        //
        target.executeWithRetry(mockHttpClient, testRequest, mockPostRequestOp);

        //
        // Validate
        //
        Mockito.verify(mockHttpClient, Mockito.times(1)).execute(testRequest);
    }

    @Test
    public void testExecuteWithRetry() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockPostRequestOp.process(testRequest, mockResponse)).thenReturn(true);

        //
        // Execute
        //
        target.executeWithRetry(mockHttpClient, testRequest, mockPostRequestOp);

        //
        // Validate
        //
        Mockito.verify(mockHttpClient, Mockito.times(2)).execute(testRequest);
    }
}