package ru.yandex.practicum.stats.analyzer.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.stats.analyzer.model.UserAction;

import java.util.List;
import java.util.Optional;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserAction> findByEventId(Long eventId);

    List<UserAction> findByEventIdIn(Iterable<Long> eventId);

    @Query("select action.eventId " +
            "from UserAction action " +
            "where action.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Query("select action " +
            "from UserAction action " +
            "where action.userId = :userId " +
            "order by action.timestamp desc")
    List<UserAction> findByUserIdOrderByTimestamp(@Param("userId") Long userId);
}
