package com.cdcrane.social_konnect_backend.feeds;

import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "following_feed_items")
@EntityListeners(AuditingEntityListener.class) // Need this for JPA auditing, allows the @CreatedDate annotation to work.
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class FollowingFeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "feed_owner_id")
    private ApplicationUser feedOwner;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @CreatedDate
    private Instant createdAt;
}
