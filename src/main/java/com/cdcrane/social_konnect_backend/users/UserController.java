package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.JWTUtil;
import com.cdcrane.social_konnect_backend.users.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserUseCase userUseCase;
    private final JWTUtil jWTUtil;

    @Autowired
    public UserController(UserService userService, JWTUtil jWTUtil) {
        this.userUseCase = userService;
        this.jWTUtil = jWTUtil;
    }

    /* This controller will not have the create user method, it will be in the auth controller as registerUser() */

    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers(){

        var users = userUseCase.getAllUsers();

        List<UserSummaryDTO> userSummaries = users.stream()
                .map(user -> new UserSummaryDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio(), user.getProfilePictureUrl()))
                .toList();

        return ResponseEntity.ok(userSummaries);

    }

    @GetMapping("/search/{username}")
    public ResponseEntity<Page<UserSummaryDTO>> searchUsers(@PathVariable @NotBlank String username, Pageable pageable){

        Page<ApplicationUser> users = userUseCase.searchUsersByUsername(username, pageable);

        var response = users.map(this::mapToUserSummary);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{username}")
    public ResponseEntity<UserSummaryDTO> getUserByUsername(@PathVariable @NotBlank String username){

        ApplicationUser user = userUseCase.getUserByUsernameOnlyUserSummary(username);

        return ResponseEntity.ok(mapToUserSummary(user));

    }

    @GetMapping("/metadata/{username}")
    public ResponseEntity<UserMetadataDTO> getUserMetadataByUsername(@PathVariable @NotBlank String username){

        UserMetadataDTO metadata = userUseCase.getUserMetadataByUsername(username);

        return ResponseEntity.ok(metadata);

    }

    @GetMapping("/metadata/me")
    public ResponseEntity<UserMetadataDTO> getCurrentUserMetadataByUsername(){

        UserMetadataDTO metadata = userUseCase.getCurrentUserMetadata();

        return ResponseEntity.ok(metadata);

    }

    @GetMapping("/pfp/{username}")
    public ResponseEntity<UsernameAndPfpDTO> getProfilePictureByUsername(@PathVariable @NotBlank String username){

        String url = userUseCase.getProfilePictureUrlByUsername(username);

        return ResponseEntity.ok(new UsernameAndPfpDTO(username, url));

    }

    @PutMapping
    public ResponseEntity<UserSummaryDTO> updateUserProfile(@ModelAttribute ChangeBioAndPfpDTO dto){

        ApplicationUser updated = userUseCase.changeProfileData(dto);

        return ResponseEntity.ok(mapToUserSummary(updated));

    }


    @PutMapping("/username")
    public ResponseEntity<UserSummaryDTO> updateUserName(@RequestBody @Valid UpdateUsernameDTO dto){

        ApplicationUser updated = userUseCase.updateUserName(dto.newName());

        var response = new UserSummaryDTO(updated.getId(), updated.getUsername(), updated.getEmail(), updated.getBio(), updated.getProfilePictureUrl());

        // We have to update the auth, since the old JWT will no longer be valid because it has the old username, the client must swap the JWT.
        Authentication updatedAuth = new UsernamePasswordAuthenticationToken(updated.getUsername(), null, updated.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority())).toList());

        String updatedJwt = jWTUtil.createNewJwt(updatedAuth).token();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + updatedJwt);

        return ResponseEntity.status(200).headers(headers).body(response);

    }

    @PutMapping("/password")
    public ResponseEntity<Void> updateUserPassword(@RequestBody ChangePasswordDTO changePasswordDTO){

        userUseCase.changePassword(changePasswordDTO.oldPassword() ,changePasswordDTO.newPassword());

        return ResponseEntity.ok().build();

    }

    // --------------------------------- HELPER METHODS ---------------------------------

    private UserSummaryDTO mapToUserSummary(ApplicationUser user){

        return new UserSummaryDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio(), user.getProfilePictureUrl());

    }

}
