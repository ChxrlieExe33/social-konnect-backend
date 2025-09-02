package com.cdcrane.social_konnect_backend.likes;

import com.cdcrane.social_konnect_backend.likes.dto.LikeCountDTO;
import com.cdcrane.social_konnect_backend.users.dto.UsernameAndPfpDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeUseCase likeUseCase;

    @Autowired
    public LikeController(LikeUseCase likeUseCase) {
        this.likeUseCase = likeUseCase;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable @NotNull UUID postId){

        likeUseCase.likePost(postId);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<LikeCountDTO> getLikeCountByPostId(@PathVariable UUID postId){

        int count = likeUseCase.getLikeCountByPostId(postId);

        return ResponseEntity.ok(new LikeCountDTO(postId, count));

    }

    @GetMapping("/users/{postId}")
    public ResponseEntity<Page<UsernameAndPfpDTO>> getUsernamesWhoLikePost(@PathVariable @NotNull UUID postId, Pageable pageable){

        Page<UsernameAndPfpDTO> users = likeUseCase.getUsernamesWhoLikePost(postId, pageable);

        return ResponseEntity.ok(users);

    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlikePost(@PathVariable @NotNull UUID postId){

        likeUseCase.unlikePost(postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
