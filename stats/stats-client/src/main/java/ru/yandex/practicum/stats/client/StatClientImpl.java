package ru.yandex.practicum.stats.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class StatClientImpl implements StatClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub userActionClient;
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationClient;

    @Autowired
    public StatClientImpl(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub userActionClient,
                          @GrpcClient("analyzer") RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationClient) {
        this.userActionClient = userActionClient;
        this.recommendationClient = recommendationClient;
    }

    @Override
    public void saveView(Long eventId, Long userId) {
        Instant now = Instant.now();
        userActionClient.collectUserAction(UserActionProto.newBuilder()
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setEventId(eventId)
                .setUserId(userId)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build());
    }

    @Override
    public void saveLike(Long eventId, Long userId) {
        Instant now = Instant.now();
        userActionClient.collectUserAction(UserActionProto.newBuilder()
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .setEventId(eventId)
                .setUserId(userId)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build());
    }

    @Override
    public void saveRequest(Long eventId, Long userId) {
        Instant now = Instant.now();
        userActionClient.collectUserAction(UserActionProto.newBuilder()
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .setEventId(eventId)
                .setUserId(userId)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .build());
    }

    @Override
    public Map<Long, Double> fetchScore(Iterable<Long> ids) {
        Iterator<RecommendedEventProto> interactionCount = recommendationClient.getInteractionsCount(InteractionsCountRequestProto.newBuilder()
                .addAllEventId(ids)
                .build());
        Map<Long, Double> result = new HashMap<>();
        interactionCount.forEachRemaining(proto -> result.put(proto.getEventId(), proto.getScore()));
        return result;
    }

    @Override
    public Map<Long, Double> fetchRecommendations(Long userId, Long maxResults) {
        Iterator<RecommendedEventProto> recommendedEvents = recommendationClient.getRecommendationsForUser(UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build());
        Map<Long, Double> result = new HashMap<>();
        recommendedEvents.forEachRemaining(proto -> result.put(proto.getEventId(), proto.getScore()));
        return result;
    }
}
