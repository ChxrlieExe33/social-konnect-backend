package com.cdcrane.social_konnect_backend.posts;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello(){
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/hidden_hello")
    public ResponseEntity<String> sayHiddenHello(){
        return ResponseEntity.ok("Hello World, this is a hidden endpoint \nWelcome to Social Konnect: "
                                + SecurityContextHolder.getContext().getAuthentication().getName() + "\nWith roles: "
                                + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }

}
