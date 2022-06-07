package org.opennms.horizon.core.monitor;

import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.horizon.echo.monitor.SimpleMinionRpcMonitor;
import org.opennms.horizon.ipc.rpc.api.RpcClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MinionRpcMonitorManager {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MinionRpcMonitorManager.class);

    private Logger log = DEFAULT_LOGGER;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private RpcClientFactory rpcClientFactory;

    private final Map<String, ScheduledFuture<?>> tasks = new HashMap<>();
    private final Object lock = new Object();
    private final AtomicLong threadNumber = new AtomicLong(0);

    private int monitorInitialDelay = 3_000;
    private int monitorPeriod = 90_000;

//========================================
// Getters and Setters
//----------------------------------------

    public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public void setScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
    }

    public RpcClientFactory getRpcClientFactory() {
        return rpcClientFactory;
    }

    public void setRpcClientFactory(RpcClientFactory rpcClientFactory) {
        this.rpcClientFactory = rpcClientFactory;
    }

//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        if (scheduledThreadPoolExecutor == null) {
            ThreadFactory threadFactory =
                    runnable -> {
                        Thread result = new Thread(runnable, "minion-monitor-" + threadNumber.incrementAndGet());
                        result.setDaemon(true);
                        return result;
                    };

            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, threadFactory);
        }
    }

//========================================
// Operations
//----------------------------------------

    public void startMonitorMinion(MinionInfo minionInfo) {
        log.info("Request to start monitor for minion: id={}; location={}", minionInfo.getId(), minionInfo.getLocation());

        // This critical section is doing a little more than ideal, but it should be fast regardless.
        synchronized (lock) {
            if (tasks.containsKey(minionInfo.getId())) {
                log.info("Minion already being monitored: id={}", minionInfo.getId());
                return;
            }

            ScheduledFuture<?> future =
                    scheduledThreadPoolExecutor
                            .scheduleAtFixedRate(() -> executeMinionCheck(minionInfo), monitorInitialDelay, monitorPeriod, TimeUnit.MILLISECONDS);

            tasks.put(minionInfo.getId(), future);
        }

        log.info("Started monitor for minion: id={}; location={}", minionInfo.getId(), minionInfo.getLocation());
    }

    public void stopMonitorMinion(String minionId) {
        log.info("Stopping monitor for minion: id={}", minionId);

        ScheduledFuture<?> future;
        synchronized (lock) {
            future = tasks.remove(minionId);
        }

        if (future != null) {
            future.cancel(true);
        }
    }

//========================================
// Internals
//----------------------------------------

    private void executeMinionCheck(MinionInfo minionInfo) {
        log.info("Minion RPC Monitor: executing check for minion: id={}; location={}", minionInfo.getId(), minionInfo.getLocation());

        try {
            SimpleMinionRpcMonitor monitor = new SimpleMinionRpcMonitor();
            monitor.setNodeId(minionInfo.getId());
            monitor.setNodeLocation(minionInfo.getLocation());
            monitor.setRpcClientFactory(rpcClientFactory);

            // TBD888: how to get real values for these?
            monitor.setAddress(InetAddress.getLocalHost());
            monitor.setNodeLabel("TBD888-NODE-LABEL");

            monitor.process();
        } catch (Exception exc) {
            log.error("Minion RPC Monitor: check for minion failed: id={}", minionInfo.getId(), exc);
        }
    }
}
