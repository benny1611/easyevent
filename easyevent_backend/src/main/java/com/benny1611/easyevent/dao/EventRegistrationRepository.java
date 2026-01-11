package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);
}
