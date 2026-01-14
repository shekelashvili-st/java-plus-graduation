package ru.yandex.practicum.core.main.service.views.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.core.main.service.views.model.EventView;

public interface EventViewRepository extends JpaRepository<EventView, Long> {
    boolean existsByIpAndEventId(String ip, Long eventId);

    Long countByEventId(Long eventId);
}

