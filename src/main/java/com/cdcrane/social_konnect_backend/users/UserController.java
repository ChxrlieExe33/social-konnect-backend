package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.JWTUtil;
import com.cdcrane.social_konnect_backend.users.dto.UpdateUsernameDTO;
import com.cdcrane.social_konnect_backend.users.dto.UserSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final UserService userService;
    private final JWTUtil jWTUtil;

    @Autowired
    public UserController(UserService userService, JWTUtil jWTUtil) {
        this.userService = userService;
        this.jWTUtil = jWTUtil;
    }

    /* This controller will not have the create user method, it will be in the auth controller as registerUser() */

    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers(){

        var users = userService.getAllUsers();

        List<UserSummaryDTO> userSummaries = users.stream()
                .map(user -> new UserSummaryDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio()))
                .toList();

        return ResponseEntity.ok(userSummaries);

    }

    @PutMapping("/username")
    public ResponseEntity<UserSummaryDTO> updateUserName(@RequestBody UpdateUsernameDTO dto){

        ApplicationUser updated = userService.updateUserName(dto.oldName(), dto.newName());

        var response = new UserSummaryDTO(updated.getId(), updated.getUsername(), updated.getEmail(), updated.getBio());

        // We have to update the auth, since the old JWT will no longer be valid because it has the old username, the client must swap the JWT.
        Authentication updatedAuth = new UsernamePasswordAuthenticationToken(updated.getUsername(), null, updated.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority())).toList());

        String updatedJwt = jWTUtil.createNewJwt(updatedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + updatedJwt);

        return ResponseEntity.status(200).headers(headers).body(response);

    }


}
