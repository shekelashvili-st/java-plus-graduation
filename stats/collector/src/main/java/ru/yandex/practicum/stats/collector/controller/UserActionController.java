package ru.yandex.practicum.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.stats.collector.service.UserActionHandler;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionHandler handler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> response) {
        log.info("Entering controller method {}\n with args {}", "collectUserAction", request);
        try {
            handler.handle(request);
            response.onNext(Empty.getDefaultInstance());
            response.onCompleted();
            log.info("Exiting controller method {}", "collectUserAction");
        } catch (Exception e) {
            log.error("Exception occurred in controller method {}\n, {}", "collectUserAction", e.toString());
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
