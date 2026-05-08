package ru.yandex.practicum.stats.analyzer.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("select es " +
            "from EventSimilarity es " +
            "where es.eventA = :eventId " +
            "or es.eventB = :eventId " +
            "order by es.score desc")
    List<EventSimilarity> findByEventAIsOrEventBIsOrderByScoreDesc(@Param("eventId") Long eventId);

    @Query("select es " +
            "from EventSimilarity es " +
            "where es.eventA in :eventIds " +
            "or es.eventB in :eventIds " +
            "order by es.score desc")
    List<EventSimilarity> findByEventAInOrEventBInOrderByScoreDesc(@Param("eventIds") Iterable<Long> eventIds);
}
