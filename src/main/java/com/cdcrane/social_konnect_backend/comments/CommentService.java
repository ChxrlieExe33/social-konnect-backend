package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.comments.dto.AddCommentDTO;
import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentService implements CommentUseCase {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final SecurityUtils securityUtils;

    @Autowired
    public CommentService(PostRepository postRepository, CommentRepository commentRepository, SecurityUtils securityUtils) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional
    public Comment addCommentToPostByPostId(AddCommentDTO addCommentDTO, UUID postId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = securityUtils.getCurrentAuth();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post with id " + postId + " not found, cannot add comment."));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(TextInputValidator.removeHtmlTagsAllowBasic(addCommentDTO.content()))
                .build();

        comment.setUser(user);
        comment.setPost(post);

        return comment = commentRepository.save(comment);

    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with id " + commentId + " not found, cannot delete comment."));

        if (comment.getUser().getId() != user.getId()) {
            throw new ActionNotPermittedException("User " + user.getUsername() + " is not allowed to delete comment with id "
                    + commentId
                    + " as it does not belong to them. (Only the user who created the comment can delete it.)");
        }

        commentRepository.delete(comment);
    }
}
