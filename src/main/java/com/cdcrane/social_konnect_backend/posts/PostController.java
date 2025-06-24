package com.cdcrane.social_konnect_backend.posts;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @GetMapping
    public ResponseEntity<String> seyHello(){
        return ResponseEntity.ok("Hello World");
    }

}
