package com.securevault.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.SecurityEvent;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByUserIdOrderByEventTimeDesc(Long userId);


    long countByUserIdAndEventTypeAndLoginStatusAndEventTimeBetween(
        Long userId,
        SecurityEvent.EventType eventType,
        SecurityEvent.LoginStatus loginStatus,
        LocalDateTime from,
        LocalDateTime to
);

List<SecurityEvent> findTop10ByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
        Long userId,
        LocalDateTime from,
        LocalDateTime to
);
List<SecurityEvent> findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
        Long userId,
        LocalDateTime from,
        LocalDateTime to
);

}
