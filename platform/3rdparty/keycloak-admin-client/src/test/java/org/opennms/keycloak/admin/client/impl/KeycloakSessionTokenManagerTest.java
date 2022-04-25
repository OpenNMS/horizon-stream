package org.opennms.keycloak.admin.client.impl;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenOp;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class KeycloakSessionTokenManagerTest extends TestCase {

    private KeycloakSessionTokenManager target;

    private RefreshTokenOp mockRefreshTokenOp;
    private AtomicInteger tokenCounter = new AtomicInteger(1);

    @Override
    public void setUp() throws Exception {
        target = new KeycloakSessionTokenManager("x-access-token-1-x", "x-refresh-token-1-x");

        mockRefreshTokenOp = Mockito.mock(RefreshTokenOp.class);
    }

    public void testRefreshAlreadyHasNewToken() {
        String newToken = target.refreshToken("x-access-token-0-x", mockRefreshTokenOp);

        assertEquals("x-access-token-1-x", newToken);
        Mockito.verifyNoInteractions(mockRefreshTokenOp);
    }

    public void testRefreshSingleThreadNeedsNewToken() {
        Mockito.when(mockRefreshTokenOp.refreshToken()).thenReturn(new RefreshTokenResponse("x-access-token-2-x", "x-refresh-token-2-x"));
        String newToken = target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);

        assertEquals("x-access-token-2-x", newToken);
    }

    @Test(timeout = 60000)
    public void testRefreshMultipleThreadsNeedNewToken() throws Exception {
        int numThread = 10;
        CountDownLatch testLatch1 = new CountDownLatch(numThread);
        AtomicInteger callCounter = new AtomicInteger(0);
        String[] responses = new String[numThread];
        Thread[] testThreads = new Thread[numThread];

        Mockito.when(mockRefreshTokenOp.refreshToken())
                .thenAnswer(invocationOnMock -> testRefreshOp(testLatch1, callCounter));

        //
        // Startup all of the threads.
        //
        int cur = 0;
        while (cur < numThread) {
            final int finalPos = cur;
            Thread thread = new Thread(() -> {
                // Count down the thread-startup latch
                testLatch1.countDown();
                responses[finalPos] = target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);
            });

            testThreads[cur] = thread;

            thread.start();
            cur++;
        }


        //
        // Wait for the threads to all startup
        //
        testLatch1.await();


        //
        // Now join all the threads and confirm they all returned the same token, x-access-token-1-x.
        //
        cur = 0;
        while (cur < numThread) {
            testThreads[cur].join();

            assertEquals("xx-access-token-2-xx", responses[cur]);

            cur++;
        }


        //
        // Verify the refresh operation was only called once.
        //
        assertEquals(1, callCounter.get());
    }

    @Test(timeout = 60000)
    public void testRefreshMultipleInSequence() throws Exception {
        CountDownLatch testLatch1 = new CountDownLatch(0);
        AtomicInteger callCounter = new AtomicInteger(0);

        Mockito.when(mockRefreshTokenOp.refreshToken())
                .thenAnswer(invocationOnMock -> testRefreshOp(testLatch1, callCounter));

        String result;

        result = target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);
        assertEquals("xx-access-token-2-xx", result);

        result = target.refreshToken(result, mockRefreshTokenOp);
        assertEquals("xx-access-token-3-xx", result);


        //
        // Verify the refresh operation was only called once.
        //
        assertEquals(2, callCounter.get());
    }

    @Test(timeout = 60000)
    public void testRefreshMultipleThreadsException() throws Exception {
        int numThread = 10;
        CountDownLatch testLatch1 = new CountDownLatch(numThread);
        AtomicInteger callCounter = new AtomicInteger(0);
        Exception[] exceptions = new Exception[numThread];
        Thread[] testThreads = new Thread[numThread];

        Mockito.when(mockRefreshTokenOp.refreshToken())
                .thenAnswer(invocationOnMock -> testRefreshExceptionOp(testLatch1, callCounter));

        //
        // Startup all of the threads.
        //
        int cur = 0;
        while (cur < numThread) {
            final int finalPos = cur;
            Thread thread = new Thread(() -> {
                // Count down the thread-startup latch
                try {
                    testLatch1.countDown();
                    target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);
                } catch (Exception exc) {
                    exceptions[finalPos] = exc;
                }
            });

            testThreads[cur] = thread;

            thread.start();
            cur++;
        }


        //
        // Wait for the threads to all startup
        //
        testLatch1.await();


        //
        // Now join all the threads and confirm they all logged the exception.
        //
        cur = 0;
        while (cur < numThread) {
            testThreads[cur].join();

            assertNotNull(exceptions[cur]);

            cur++;
        }


        //
        // Verify the refresh operation was only called once.
        //
        assertEquals(1, callCounter.get());
    }

    @Test(timeout = 60000)
    public void testRefreshWaitingThreadInterruptedException() throws Exception {
        CountDownLatch testLatch1 = new CountDownLatch(1);
        AtomicInteger callCounter = new AtomicInteger(0);
        Exception[] exceptions = new Exception[1];

        Thread thisThread = Thread.currentThread();
        Mockito.when(mockRefreshTokenOp.refreshToken())
                .thenAnswer(invocationOnMock -> testRefreshInterruptPeerOp(testLatch1, thisThread, callCounter));

        //
        // Startup the peer thread that will execute the refresh.
        //
        Thread peerThread = new Thread(() -> {
            // Count down the thread-startup latch
            try {
                testLatch1.countDown();
                target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);
            } catch (Exception exc) {
            }
        });

        peerThread.start();


        //
        // Wait for the threads to all startup
        //
        testLatch1.await();


        //
        // Directly call the refresh from this thread and wait for the interruption.
        //
        try {
            target.refreshToken("x-access-token-1-x", mockRefreshTokenOp);
            fail("missing expected exception");
        } catch (RuntimeException exc) {
            assertEquals("error on waiting for refresh of the current session token", exc.getMessage());
            assertTrue(exc.getCause() instanceof InterruptedException);
        }


        //
        // Verify the refresh operation was only called once.
        //
        assertEquals(1, callCounter.get());
    }


//========================================
// Internals
//----------------------------------------

    private RefreshTokenResponse testRefreshOp(CountDownLatch countDownLatch, AtomicInteger numCall) {
        try {
            numCall.incrementAndGet();
            boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);

            if (! completed) {
                throw new RuntimeException("timed out waiting for threads to startup");
            }

            // Give the other threads enough time to get to the point of waiting.  This isn't perfect, so it is possible
            //  to have false positives (i.e. for the test to fail when it shouldn't).  We could perfect this by adding
            //  a test hook to the implementation.
            Thread.sleep(100);
        } catch (InterruptedException intExc) {
            throw new RuntimeException("test interrupted", intExc);
        }

        int counter = tokenCounter.incrementAndGet();
        return new RefreshTokenResponse("xx-access-token-" + counter + "-xx", "xx-refresh-token-" + counter + "-xx");
    }

    private RefreshTokenResponse testRefreshExceptionOp(CountDownLatch countDownLatch, AtomicInteger numCall) {
        try {
            numCall.incrementAndGet();
            boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);

            if (! completed) {
                throw new RuntimeException("timed out waiting for threads to startup");
            }

            // Give the other threads enough time to get to the point of waiting.  This isn't perfect, so it is possible
            //  to have false positives (i.e. for the test to fail when it shouldn't).  We could perfect this by adding
            //  a test hook to the implementation.
            Thread.sleep(100);
        } catch (InterruptedException intExc) {
            throw new RuntimeException("test interrupted", intExc);
        }

        throw new RuntimeException("test failure");
    }

    private RefreshTokenResponse testRefreshInterruptPeerOp(CountDownLatch countDownLatch, Thread peerThread, AtomicInteger numCall) {
        try {
            numCall.incrementAndGet();
            boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);

            if (! completed) {
                throw new RuntimeException("timed out waiting for threads to startup");
            }

            // Give the other threads enough time to get to the point of waiting.  This isn't perfect, so it is possible
            //  to have false positives (i.e. for the test to fail when it shouldn't).  We could perfect this by adding
            //  a test hook to the implementation.
            Thread.sleep(100);
        } catch (InterruptedException intExc) {
            throw new RuntimeException("test interrupted", intExc);
        }

        peerThread.interrupt();

        int counter = tokenCounter.incrementAndGet();
        return new RefreshTokenResponse("xx-access-token-" + counter + "-xx", "xx-refresh-token-" + counter + "-xx");
    }
}