package com.cdcrane.social_konnect_backend.posts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p ORDER BY p.postedAt DESC ")
    Page<Post> getPostsOrderByPostedAt(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.username = ?1 ORDER BY p.postedAt DESC")
    Page<Post> getPostsByUsernameOrderByPostedAt(String username, Pageable pageable);
}
