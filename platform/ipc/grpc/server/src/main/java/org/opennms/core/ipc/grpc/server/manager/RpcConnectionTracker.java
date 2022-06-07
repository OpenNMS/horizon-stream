package org.opennms.core.ipc.grpc.server.manager;

import io.grpc.stub.StreamObserver;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;

public interface RpcConnectionTracker {
    /**
     *     private Map<String, StreamObserver<RpcRequestProto>> rpcHandlerByMinionId = new HashMap<>();
     */

    boolean addConnection(String location, String  minionId, StreamObserver<RpcRequestProto> connection);
    StreamObserver<RpcRequestProto> lookupByMinionId(String minionId);
    StreamObserver<RpcRequestProto> lookupByLocationRoundRobin(String locationId);
    MinionInfo removeConnection(StreamObserver<RpcRequestProto> connection);

    void clear();
}
