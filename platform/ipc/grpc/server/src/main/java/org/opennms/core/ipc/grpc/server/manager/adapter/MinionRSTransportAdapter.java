package org.opennms.core.ipc.grpc.server.manager.adapter;

import io.grpc.stub.StreamObserver;
import org.opennms.core.ipc.grpc.common.Empty;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.common.RpcResponseProto;
import org.opennms.core.ipc.grpc.common.SinkMessage;

import java.util.function.Function;

public class MinionRSTransportAdapter extends OpenNMSIpcGrpc.OpenNMSIpcImplBase {

    private final Function<StreamObserver<RpcRequestProto>, StreamObserver<RpcResponseProto>> startRpcStreamingProcessor;
    private final Function<StreamObserver<Empty>, StreamObserver<SinkMessage>> startSinkStreamingProcessor;

    public MinionRSTransportAdapter(
            Function<StreamObserver<RpcRequestProto>, StreamObserver<RpcResponseProto>> startRpcStreamingProcessor,
            Function<StreamObserver<Empty>, StreamObserver<SinkMessage>> startSinkStreamingProcessor) {

        this.startRpcStreamingProcessor = startRpcStreamingProcessor;
        this.startSinkStreamingProcessor = startSinkStreamingProcessor;
    }

    @Override
    public StreamObserver<RpcResponseProto> rpcStreaming(
            StreamObserver<RpcRequestProto> responseObserver) {

        return startRpcStreamingProcessor.apply(responseObserver);
    }

    @Override
    public StreamObserver<SinkMessage> sinkStreaming(
            StreamObserver<Empty> responseObserver) {

        return startSinkStreamingProcessor.apply(responseObserver);
    }
}
