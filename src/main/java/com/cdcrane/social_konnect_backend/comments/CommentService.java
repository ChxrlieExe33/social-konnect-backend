package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.comments.dto.AddCommentDTO;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
import com.cdcrane.social_konnect_backend.posts.Post;
import com.cdcrane.social_konnect_backend.posts.PostRepository;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentService implements CommentUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Comment addCommentToPostByPostId(AddCommentDTO addCommentDTO, UUID postId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        if (user == null) {
            throw new UserNotFoundException("User with username " + username + " not found");
        }

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
    public void deleteComment(Comment comment) {

    }
}
