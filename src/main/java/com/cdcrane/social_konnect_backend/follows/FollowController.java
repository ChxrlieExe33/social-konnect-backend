package com.cdcrane.social_konnect_backend.follows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowUseCase followUseCase;

    @Autowired
    public FollowController(FollowService followService) {
        this.followUseCase = followService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<Void> followUser(@PathVariable String username){

        this.followUseCase.followUser(username);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> unfollowUser(@PathVariable String username){

        this.followUseCase.unfollowUser(username);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
