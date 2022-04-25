package org.opennms.keycloak.admin.client.impl;

import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenOp;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 * Manages the Access and Refresh tokens for a Keycloak session, including logic to update the tokens by calling out to
 * the Keycloak server when requested.  Note that this manager sits below the Client Session Impl in the stack, and does
 * not create nor execute HTTP requests itself; instead, the caller must provide a RefreshTokenOp callback that performs
 * the HTTP request.
 *
 * This structure prevents a circular dependency between this class and the Client Session Impl while enabling this
 * logic to be extracted for easier maintenance and testing.
 *
 * NOTE: this class handles concurrent requests to refresh the token, combining multiple requests into one, and comparing
 * the prior token of the requester against the current token for the session and only requesting a new one when needed.
 */
public class KeycloakSessionTokenManager {

    private final Object lock = new Object();

    private String accessToken;
    private String refreshToken;
    private CountDownLatch refreshLatch;
    private Exception lastRefeshException = null;

    /**
     * Create the manager with the initial tokens given.
     *
     * @param accessToken
     * @param refreshToken
     */
    public KeycloakSessionTokenManager(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Request a refresh of the Access Token now, with the given RefreshTokenOp that performs the actuall HTTP callout
     * to Keycloak.
     *
     * Note there are 3 main success scenarios:
     *  1. There is an ongoing refresh operation executing concurrently
     *      - The calling thread waits for that refresh operation to complete and uses the result of that operation.
     *  2. There is no ongoing refresh operation and the caller currently has the latest access token.
     *      - In this case, the calling thread executes the refresh operation and, on completion, notifies any waiting
     *        peers.
     *  3. There is no ongoing refresh operation and there already is a new token available for the caller.
     *      - In this case, the new token is returned to the caller.
     *
     * When an exception is thrown by the refresh operation, all threads waiting for the one refresh operation throw
     *  that same exception.
     *
     * @param priorAccessToken the prior access token from this session used by the caller; used to detect when a new
     *                         access token is already available without requiring a callout to Keycloak.
     * @param refreshCallout the callout that actually sends the HTTP request to Keycloak.
     * @return the new access token
     */
    public String refreshToken(String priorAccessToken, RefreshTokenOp refreshCallout) {
        CountDownLatch activeLatch = null;
        boolean initiateRefresh = false;

        //
        // Critical Section: keep it short and quick
        //
        synchronized (lock) {
            // If there is an ongoing refresh, wait for it to complete and use the result of that refresh.
            if (refreshLatch != null) {
                activeLatch = refreshLatch;
            } else {
                // No ongoing refresh.  Is the current token the same one the caller used last?  If so, we need a
                //  refresh; if not, just return the latest token.
                if (accessToken.equals(priorAccessToken)) {
                    initiateRefresh = true;
                    refreshLatch = new CountDownLatch(1);
                } else {
                    // DONE (already have a newer token)
                    return accessToken;
                }
            }
        }

        // Do we need to initiate the refresh?
        if (initiateRefresh) {
            try {
                RefreshTokenResponse refreshTokenResponse = refreshCallout.refreshToken();
                // CRITICAL SECTION
                synchronized (lock) {
                    accessToken = refreshTokenResponse.getAccessToken();
                    refreshToken = refreshTokenResponse.getRefreshToken();
                }
            } catch (Exception exc) {
                lastRefeshException = exc;
                throw new RuntimeException("refresh attempt failed", exc);
            } finally {
                //
                // Make sure the latch is counted down, even after an exception.  Otherwise, other concurrent calls
                //  linger indefinitely after the failure.
                //

                // CRITICAL SECTION
                synchronized (lock) {
                    refreshLatch.countDown();
                    refreshLatch = null;
                }
            }
        } else {
            try {
                // TODO: timeout?
                activeLatch.await();

                // If the last refresh attempt threw an exception, rethrow it.
                Exception exc = lastRefeshException;
                if (exc != null) {
                    throw new RuntimeException("peer thread's concurrent refresh attempt failed", exc);
                }
            } catch (InterruptedException intExc) {
                throw new RuntimeException("error on waiting for refresh of the current session token", intExc);
            }
        }

        return accessToken;
    }
}
