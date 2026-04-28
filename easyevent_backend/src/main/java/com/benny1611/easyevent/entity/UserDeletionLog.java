package com.benny1611.easyevent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_deletion_log")
@Data
public class UserDeletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "deletion_type")
    private String deletionType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "occurred_at")
    private OffsetDateTime occurredAt;

}
