package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    Optional<EventRegistration> findByEventIdAndGuestId(Long eventId, Long guestId);
}
