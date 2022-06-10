package org.opennms.core.ipc.grpc.server.manager.rpcstreaming;

import org.opennms.core.ipc.grpc.common.RpcResponseProto;

public interface MinionRpcStreamConnection {
    void handleRpcStreamInboundMessage(RpcResponseProto message);

    void handleRpcStreamInboundError(Throwable thrown);

    void handleRpcStreamInboundCompleted();
}
