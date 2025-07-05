package com.cdcrane.social_konnect_backend.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* This controller will not have the create user method, it will be in the auth controller as registerUser() */

    @GetMapping
    public ResponseEntity<Page<ApplicationUser>> getAllUsers(Pageable pageable){

        Page<ApplicationUser> p = userService.getAllUsersPaginated(pageable);

        return ResponseEntity.ok(p);

    }

}
