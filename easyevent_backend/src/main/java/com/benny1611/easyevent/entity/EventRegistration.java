package com.benny1611.easyevent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "event_registrations",
uniqueConstraints = {
        @UniqueConstraint(name = "ux_event_user", columnNames = {"event_id", "user_id"}),
        @UniqueConstraint(name = "ux_event_guest", columnNames = {"event_id", "guest_id"})
})
@Data
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(name = "registered_at", nullable = false)
    private String registeredAt = Instant.now().toString();

    public boolean isGuestRegistration() {
        return guest != null;
    }

    public boolean isUserRegistration() {
        return user != null;
    }
}
