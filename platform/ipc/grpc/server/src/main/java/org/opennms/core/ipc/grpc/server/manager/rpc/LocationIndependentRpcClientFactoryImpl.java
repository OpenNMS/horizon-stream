package org.opennms.core.ipc.grpc.server.manager.rpc;


import com.codahale.metrics.MetricRegistry;
import org.opennms.core.ipc.grpc.server.manager.LocationIndependentRpcClientFactory;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.ipc.rpc.api.RpcModule;
import org.opennms.horizon.ipc.rpc.api.RpcRequest;
import org.opennms.horizon.ipc.rpc.api.RpcResponse;

public class LocationIndependentRpcClientFactoryImpl implements LocationIndependentRpcClientFactory {

    private Identity serverIdentity;
    private TracerRegistry tracerRegistry;
    private MetricRegistry rpcMetrics;
    private long ttl;

    private RpcConnectionTracker rpcConnectionTracker;


//========================================
// Getters and Setters
//----------------------------------------

    public Identity getServerIdentity() {
        return serverIdentity;
    }

    public void setServerIdentity(Identity serverIdentity) {
        this.serverIdentity = serverIdentity;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public MetricRegistry getRpcMetrics() {
        return rpcMetrics;
    }

    public void setRpcMetrics(MetricRegistry rpcMetrics) {
        this.rpcMetrics = rpcMetrics;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public RpcConnectionTracker getRpcConnectionTracker() {
        return rpcConnectionTracker;
    }

    public void setRpcConnectionTracker(RpcConnectionTracker rpcConnectionTracker) {
        this.rpcConnectionTracker = rpcConnectionTracker;
    }


//========================================
// Operations
//----------------------------------------

    @Override
    public <REQUEST extends RpcRequest, RESPONSE extends RpcResponse> LocationIndependentRpcClient<REQUEST, RESPONSE>
    createClient(
            RpcModule<REQUEST, RESPONSE> localModule,
            RemoteRegistrationHandler remoteRegistrationHandler
    ) {
        LocationIndependentRpcClient<REQUEST, RESPONSE> result =
                new LocationIndependentRpcClient<>(localModule, remoteRegistrationHandler);

        result.setServerIdentity(serverIdentity);
        result.setTracerRegistry(tracerRegistry);
        result.setRpcMetrics(rpcMetrics);
        result.setTtl(ttl);
        result.setRpcConnectionTracker(rpcConnectionTracker);

        return result;
    }
}
