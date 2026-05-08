package ru.yandex.practicum.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.yandex.practicum.stats.analyzer.service.RecommendationsService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService service;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> response) {
        log.info("Entering controller method {}\n with args {}", "getRecommendationsForUser", request);
        try {
            service.getRecommendationsForUser(request).forEach(response::onNext);
            response.onCompleted();
            log.info("Exiting controller method {}", "getRecommendationsForUser");
        } catch (Exception e) {
            log.error("Exception occurred in controller method {}\n, {}", "getRecommendationsForUser", e.toString());
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> response) {
        log.info("Entering controller method {}\n with args {}", "getSimilarEvents", request);
        try {
            service.getSimilarEvents(request).forEach(response::onNext);
            response.onCompleted();
            log.info("Exiting controller method {}", "getSimilarEvents");
        } catch (Exception e) {
            log.error("Exception occurred in controller method {}\n, {}", "getSimilarEvents", e.toString());
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> response) {
        log.info("Entering controller method {}\n with args {}", "getInteractionsCount", request);
        try {
            service.getInteractionsCount(request).forEach(response::onNext);
            response.onCompleted();
            log.info("Exiting controller method {}", "getInteractionsCount");
        } catch (Exception e) {
            log.error("Exception occurred in controller method {}\n, {}", "getInteractionsCount", e.toString());
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
