package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO;
import com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p ORDER BY p.postedAt DESC ")
    Page<Post> getPostsOrderByPostedAt(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.username = ?1 ORDER BY p.postedAt DESC")
    Page<Post> getPostsByUsernameOrderByPostedAt(String username, Pageable pageable);

    int countByUserId(long userId);

    @Query("""
        SELECT new com.cdcrane.social_konnect_backend.posts.dto.PostMetadataDTO(
            p.id,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id)
        )
        FROM Post p
        LEFT JOIN Comment c ON c.post = p
        LEFT JOIN Like l ON l.post = p
        WHERE p.id = :postId
        GROUP BY p.id
    """)
    Optional<PostMetadataDTO> getPostMetadataByPostId(UUID postId);

    @Query("""
        SELECT new com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO(
            p.id,
            CASE WHEN l.id IS NOT NULL THEN true ELSE false END
        )
        FROM Post p
        LEFT JOIN Like l ON l.post.id = p.id AND l.user.id = :userId
        WHERE p.id IN :postIds
    """)
    List<PostLikeStatusDTO> findLikeStatusByPostIds(@Param("postIds") List<UUID> postIds, @Param("userId") Long userId);

    @Query("""
        SELECT new com.cdcrane.social_konnect_backend.posts.dto.PostLikeStatusDTO(
            p.id,
            CASE WHEN l.id IS NOT NULL THEN true ELSE false END
        )
        FROM Post p
        LEFT JOIN Like l ON l.post.id = p.id AND l.user.id = :userId
        WHERE p.id = :postId
    """)
    PostLikeStatusDTO findLikeStatusByPostId(@Param("postId") UUID postId, @Param("userId") Long userId);

}
