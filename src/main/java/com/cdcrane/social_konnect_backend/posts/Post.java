package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.comments.Comment;
import com.cdcrane.social_konnect_backend.likes.Like;
import com.cdcrane.social_konnect_backend.posts.post_media.PostMedia;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class) // Need this for JPA auditing, allows the @CreatedDate annotation to work.
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id")
    private UUID id;

    @CreatedDate
    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;

    private String caption;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("posts")
    private ApplicationUser user;

    @OneToMany
    @JoinColumn(name = "post_id")
    private List<PostMedia> postMedia;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private List<Comment> comments;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private List<Like> likes;

    public void addComment(Comment comment){

        if (this.comments == null){
            this.comments = new LinkedList<>();
        }

        this.comments.add(comment);

    }

    public void addLike(Like like){

        if (this.likes == null){
            this.likes = new LinkedList<>();
        }

        this.likes.add(like);

    }


}
