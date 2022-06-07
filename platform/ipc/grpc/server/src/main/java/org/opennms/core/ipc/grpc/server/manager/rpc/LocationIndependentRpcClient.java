package org.opennms.core.ipc.grpc.server.manager.rpc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.server.OpennmsGrpcServer;
import org.opennms.core.ipc.grpc.server.manager.RpcConnectionTracker;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.core.lib.Logging;
import org.opennms.horizon.ipc.rpc.api.RpcClient;
import org.opennms.horizon.ipc.rpc.api.RpcClientFactory;
import org.opennms.horizon.ipc.rpc.api.RpcModule;
import org.opennms.horizon.ipc.rpc.api.RpcRequest;
import org.opennms.horizon.ipc.rpc.api.RpcResponse;
import org.opennms.horizon.ipc.rpc.api.RpcResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;

public class LocationIndependentRpcClient<REQUEST extends RpcRequest, RESPONSE extends RpcResponse> implements RpcClient<REQUEST, RESPONSE> {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LocationIndependentRpcClient.class);

    private Logger log = DEFAULT_LOGGER;

    private Identity serverIdentity;
    private TracerRegistry tracerRegistry;
    private MetricRegistry rpcMetrics;
    private long ttl;

    private final RpcModule<REQUEST, RESPONSE> localModule;
    private final RpcResponseHandler responseHandler;
    private final RemoteRegistrationHandler remoteRegistrationHandler;
    private final RpcConnectionTracker rpcConnectionTracker;


//========================================
// Constructors
//----------------------------------------

    public LocationIndependentRpcClient(
            RpcModule<REQUEST, RESPONSE> localModule,
            RpcResponseHandler responseHandler,
            RemoteRegistrationHandler remoteRegistrationHandler,
            RpcConnectionTracker rpcConnectionTracker) {

        this.localModule = localModule;
        this.responseHandler = responseHandler;
        this.remoteRegistrationHandler = remoteRegistrationHandler;
        this.rpcConnectionTracker = rpcConnectionTracker;
    }


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


//========================================
// Operations
//----------------------------------------

    @Override
    public CompletableFuture<RESPONSE> execute(REQUEST request) {
        CompletableFuture<RESPONSE> result;

        if (request.getLocation() == null || Objects.equals(request.getLocation(), serverIdentity.getLocation())) {
            log.debug("executing RPC remotely: remote-location={}; local-location={}", request.getLocation(), serverIdentity.getLocation());
            result = executeLocally(request);
        } else {
            log.debug("executing RPC locally: remote-location={}; local-location={}", request.getLocation(), serverIdentity.getLocation());
            result = executeRemotely(request);
        }

        return result;
    }

//========================================
// Internals
//----------------------------------------

    private CompletableFuture<RESPONSE> executeLocally(REQUEST request) {
        return localModule.execute(request);
    }

    private Tracer prepareTracer() {
        if (tracerRegistry != null) {
            // TODO: is this a registry or just a factory?
            return tracerRegistry.getTracer();
        }

        return GlobalTracer.get();
    }

    /**
     * Calculate the expiration time of the given request.
     *
     * @param request
     * @return
     */
    private long calcuateExpiration(REQUEST request) {
        // Does the request have a custom TTL?
        Long timeToLive = request.getTimeToLiveMs();

        if ((timeToLive == null) || (timeToLive <= 0)) {
            // Not set, or not positive; fallback to the default.
            log.debug("request TTL not set or not positive; using default: request-ttl={}; default={}", timeToLive, ttl);
            timeToLive = ttl;
        }

        // TODO: currentTimeMillis() is susceptible to wall clock adjustments.
        long expirationTime = System.currentTimeMillis() + timeToLive;

        return expirationTime;
    }

    private CompletableFuture<RESPONSE> executeRemotely(REQUEST request) {
        final Map<String, String> loggingContext = Logging.getCopyOfContextMap();

        Tracer tracer = prepareTracer();
        Span span = tracer.buildSpan(localModule.getId()).start();

        String marshalRequest = localModule.marshalRequest(request);


        long expirationTime = this.calcuateExpiration(request);
        String rpcId = remoteRegistrationHandler.registerRemoteCall(request, expirationTime);

        RpcRequestProto.Builder builder = RpcRequestProto.newBuilder()
                .setRpcId(rpcId)
                .setLocation(request.getLocation())
                .setModuleId(localModule.getId())
                .setRpcContent(ByteString.copyFrom(marshalRequest.getBytes())
                );

        if (!Strings.isNullOrEmpty(request.getSystemId())) {
            builder.setSystemId(request.getSystemId());
        }
        addTracingInfo(request, tracer, span, builder);
        RpcRequestProto requestProto = builder.build();

        boolean succeeded = sendRequest(requestProto);

        addMetrics(request, requestProto.getSerializedSize());

        CompletableFuture<RESPONSE> future = new CompletableFuture<>();
        if (!succeeded) {
            RpcClientFactory.markFailed(rpcMetrics, request.getLocation(), localModule.getId());
            future.completeExceptionally(new RuntimeException("No minion found at location " + request.getLocation()));
            return future;
        }

        log.debug("RPC request from module: {} with RpcId:{} sent to minion at location {}",
                localModule.getId(),
                rpcId,
                request.getLocation());

        return future;
    }

    private void addMetrics(org.opennms.horizon.ipc.rpc.api.RpcRequest request, int messageLen) {
        RpcClientFactory.markRpcCount(rpcMetrics, request.getLocation(), localModule.getId());
        RpcClientFactory.updateRequestSize(rpcMetrics, request.getLocation(), localModule.getId(), messageLen);
    }

    private void addTracingInfo(RpcRequest request, Tracer tracer, Span span, RpcRequestProto.Builder builder) {
        //Add tags to span.
        span.setTag(TAG_LOCATION, request.getLocation());
        if (request.getSystemId() != null) {
            span.setTag(TAG_SYSTEM_ID, request.getSystemId());
        }
        request.getTracingInfo().forEach(span::setTag);
        TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
        // Tracer adds it's own metadata.
        tracingInfoCarrier.getTracingInfoMap().forEach(builder::putTracingInfo);
        //Add custom tags from RpcRequest.
        request.getTracingInfo().forEach(builder::putTracingInfo);
    }

    private boolean sendRequest(RpcRequestProto requestProto) {
        StreamObserver<RpcRequestProto> rpcHandler = null;

        // If a specific Minion weas requested, use it
        if (! Strings.isNullOrEmpty(requestProto.getSystemId())) {
            rpcHandler = rpcConnectionTracker.lookupByMinionId(requestProto.getSystemId());
        } else {
            rpcHandler = rpcConnectionTracker.lookupByLocationRoundRobin(requestProto.getLocation());
        }

        if (rpcHandler == null) {
            log.warn("No RPC handlers found for location: location={}; minionId={}", requestProto.getLocation(), requestProto.getSystemId());
            return false;
        }

        try {
            sendRpcRequest(rpcHandler, requestProto);
            return true;
        } catch (Throwable e) {
            log.error("Encountered exception while sending request {}", requestProto, e);
        }

        return false;
    }

    /**
     * Writing message through stream observer is not thread safe.
     */
    // TBD888 - synchronized on the entire class
    private synchronized void sendRpcRequest(StreamObserver<RpcRequestProto> rpcHandler, RpcRequestProto rpcMessage) {
        rpcHandler.onNext(rpcMessage);
    }
}
