package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.comments.dto.AddCommentDTO;

import java.util.UUID;

public interface CommentUseCase {

    Comment addCommentToPostByPostId(AddCommentDTO addCommentDTO);

    void deleteComment(UUID commentId);
}
