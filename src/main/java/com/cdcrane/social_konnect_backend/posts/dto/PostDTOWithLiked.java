package com.cdcrane.social_konnect_backend.posts.dto;

import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.post_media.dto.PostMediaDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record PostDTOWithLiked(UUID postId, String caption, List<PostMediaDTO> media, String username, Instant createdAt, String profilePictureUrl, boolean liked) {

    // Constructor for existing posts
    public PostDTOWithLiked(Post post, boolean liked) {
        this(
                post.getId(),
                post.getCaption(),
                post.getPostMedia() != null ?
                        post.getPostMedia().stream()
                                .map(media -> new PostMediaDTO(media.getMediaUrl(), media.getMediaType()))
                                .collect(Collectors.toList()) : List.of(),
                post.getUser().getUsername(),
                post.getPostedAt(),
                post.getUser().getProfilePictureUrl(),
                liked
        );
    }

}
