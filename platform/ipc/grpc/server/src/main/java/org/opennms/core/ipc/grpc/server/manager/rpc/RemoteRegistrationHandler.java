package org.opennms.core.ipc.grpc.server.manager.rpc;

import io.opentracing.Span;
import org.opennms.horizon.ipc.rpc.api.RpcRequest;

public interface RemoteRegistrationHandler {
    /**
     * Register this remote call, with the given expiration time.
     *
     * @param request request that will be sent to the remote
     * @param expiration time at which the remote call should be timed out locally
     * @return identifier for the remote call
     */
    String registerRemoteCall(RpcRequest request, long expiration, Span span);
}
