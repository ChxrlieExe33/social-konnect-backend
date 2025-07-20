package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.comments.dto.AddCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentUseCase {

    Comment addCommentToPostByPostId(AddCommentDTO addCommentDTO);

    void deleteComment(UUID commentId);

    int getCommentCountByPostId(UUID postId);

    Page<Comment> getCommentsByPostId(UUID postId, Pageable pageable);
}
