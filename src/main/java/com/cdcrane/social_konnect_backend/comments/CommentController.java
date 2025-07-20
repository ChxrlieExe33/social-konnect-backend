package com.cdcrane.social_konnect_backend.comments;

import com.cdcrane.social_konnect_backend.comments.dto.AddCommentDTO;
import com.cdcrane.social_konnect_backend.comments.dto.CommentCountDTO;
import com.cdcrane.social_konnect_backend.comments.dto.CommentDataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentUseCase commentUseCase;

    @Autowired
    public CommentController(CommentUseCase commentUseCase) {
        this.commentUseCase = commentUseCase;
    }

    @PostMapping
    public ResponseEntity<CommentDataDTO> addCommentToPostByPostId(@Valid @RequestBody AddCommentDTO addCommentDTO){

        Comment comment = commentUseCase.addCommentToPostByPostId(addCommentDTO);

        return ResponseEntity.ok(new CommentDataDTO(comment.getId(), comment.getContent(), comment.getCreatedAt(), comment.getUser().getUsername(), comment.getUser().getProfilePictureUrl()));

    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId){

        commentUseCase.deleteComment(commentId);

        return ResponseEntity.status(204).build();
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<CommentCountDTO> getCommentCountByPostId(@PathVariable UUID postId){

        int count = commentUseCase.getCommentCountByPostId(postId);

        return ResponseEntity.ok(new CommentCountDTO(postId, count));

    }

    @GetMapping("/{postId}")
    public ResponseEntity<Page<CommentDataDTO>> getCommentsByPostId(@PathVariable @NotNull UUID postId, Pageable pageable){

        Page<Comment> comments = commentUseCase.getCommentsByPostId(postId, pageable);

        Page<CommentDataDTO> response = comments.map(comment ->
                new CommentDataDTO(comment.getId(), comment.getContent(), comment.getCreatedAt(),
                        comment.getUser().getUsername(), comment.getUser().getProfilePictureUrl()));

        return ResponseEntity.ok(response);

    }
}
