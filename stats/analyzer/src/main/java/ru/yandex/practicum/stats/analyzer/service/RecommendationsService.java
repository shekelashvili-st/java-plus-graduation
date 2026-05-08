package ru.yandex.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.practicum.stats.analyzer.mapper.EventSimilarityMapper;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.storage.EventSimilarityRepository;
import ru.yandex.practicum.stats.analyzer.storage.UserActionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto requestProto) {

        Map<Long, Double> idToRatingInteracted = userActionRepository.findByUserIdOrderByTimestamp(requestProto.getUserId()).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getRating));
        if (idToRatingInteracted.isEmpty()) {
            return null;
        }

        Map<Long, List<EventSimilarity>> suggestedEventIds = new HashMap<>();
        List<EventSimilarity> eventSimilarities = eventSimilarityRepository.findByEventAInOrEventBInOrderByScoreDesc(idToRatingInteracted.keySet());
        long size = 0;
        for (var sim : eventSimilarities) {
            if (!idToRatingInteracted.containsKey(sim.getEventA())) {
                suggestedEventIds.put(sim.getEventA(), new ArrayList<>());
                size += 1;
            } else if (!idToRatingInteracted.containsKey(sim.getEventB())) {
                suggestedEventIds.put(sim.getEventB(), new ArrayList<>());
                size += 1;
            }
            if (size >= requestProto.getMaxResults()) {
                break;
            }
        }

        List<RecommendedEventProto> recommendedEvents = new ArrayList<>();
        eventSimilarityRepository.findByEventAInOrEventBInOrderByScoreDesc(suggestedEventIds.keySet())
                .forEach((sim) -> {
                    List<EventSimilarity> eventAList = suggestedEventIds.get(sim.getEventA());
                    List<EventSimilarity> eventBList = suggestedEventIds.get(sim.getEventB());
                    if (eventAList != null) {
                        eventAList.add(sim);
                    }
                    if (eventBList != null) {
                        eventBList.add(sim);
                    }
                });

        for (var entry : suggestedEventIds.entrySet()) {
            List<EventSimilarity> eventSimilaritiesForRec = entry.getValue();
            double numerator = 0D;
            double denominator = 0D;
            for (var sim : eventSimilaritiesForRec) {
                if (idToRatingInteracted.containsKey(sim.getEventA())) {
                    numerator += sim.getScore() * idToRatingInteracted.get(sim.getEventA());
                    denominator += sim.getScore();
                } else if (idToRatingInteracted.containsKey(sim.getEventB())) {
                    numerator += sim.getScore() * idToRatingInteracted.get(sim.getEventB());
                    denominator += sim.getScore();
                }
            }
            recommendedEvents.add(RecommendedEventProto.newBuilder()
                    .setEventId(entry.getKey())
                    .setScore(denominator == 0D ? 0D : numerator / denominator)
                    .build());
        }
        return recommendedEvents;
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto requestProto) {
        List<EventSimilarity> eventSim = eventSimilarityRepository.findByEventAIsOrEventBIsOrderByScoreDesc(requestProto.getEventId());
        List<Long> interactedIds = userActionRepository.findIdsByUserId(requestProto.getUserId());
        interactedIds.remove(requestProto.getEventId());
        return eventSim.stream()
                .filter(sim -> !(interactedIds.contains(sim.getEventA()) || interactedIds.contains(sim.getEventB())))
                .limit(requestProto.getMaxResults())
                .map(sim -> EventSimilarityMapper.scoreToEvent(sim, requestProto.getEventId()))
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto requestProto) {
        List<UserAction> userActions = userActionRepository.findByEventIdIn(requestProto.getEventIdList());
        Map<Long, Double> eventToInteractions = new HashMap<>();
        userActions.forEach(action -> eventToInteractions.merge(action.getEventId(), action.getRating(), Double::sum));
        return eventToInteractions.entrySet().stream().map((entry) -> RecommendedEventProto.newBuilder()
                .setEventId(entry.getKey())
                .setScore(entry.getValue())
                .build()).toList();
    }
}
