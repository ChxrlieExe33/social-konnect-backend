package com.cdcrane.social_konnect_backend.comments;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class) // Need this for JPA auditing, allows the @CreatedDate annotation to work.
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id")
    private UUID id;

    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
