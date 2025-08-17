package com.cdcrane.social_konnect_backend.follows;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
@EntityListeners(AuditingEntityListener.class) // Need this for JPA auditing, allows the @CreatedDate annotation to work.
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private ApplicationUser follower;

    @ManyToOne
    @JoinColumn(name = "followed_id", nullable = false)
    private ApplicationUser followed;

    @CreatedDate
    @Column(name = "followed_at", nullable = false, updatable = false)
    private Instant followedAt;
}
