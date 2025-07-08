package com.cdcrane.social_konnect_backend.likes;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "likes")
@EntityListeners(AuditingEntityListener.class) // Need this for JPA auditing, allows the @CreatedDate annotation to work.
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "like_id")
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

}
