package com.cdcrane.social_konnect_backend.follows;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.dto.UsernameAndPfpDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/my-followers")
    public ResponseEntity<Page<UsernameAndPfpDTO>> getMyFollowers(Pageable pageable){

        Page<ApplicationUser> followers = this.followUseCase.getMyFollowers(pageable);

        var response = followers.map(follower -> new UsernameAndPfpDTO(follower.getUsername(), follower.getProfilePictureUrl()));

        return ResponseEntity.ok(response);

    }

    @GetMapping("/my-following")
    public ResponseEntity<Page<UsernameAndPfpDTO>> getMyFollowing(Pageable pageable){

        Page<ApplicationUser> following = this.followUseCase.getMyFollowing(pageable);

        var response = following.map(follower -> new UsernameAndPfpDTO(follower.getUsername(), follower.getProfilePictureUrl()));

        return ResponseEntity.ok(response);

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
