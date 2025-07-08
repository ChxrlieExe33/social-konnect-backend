package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostService implements PostUseCase {

    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Autowired
    public PostService(PostRepository postRepo, UserRepository userRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Post> getAllPosts() {

        return this.postRepo.findAll();

    }

    @Override
    @Transactional
    public List<Post> getPostsByUsername(String username) {

        ApplicationUser user = userRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        Hibernate.initialize(user.getPosts());

        return user.getPosts();

    }

    @Override
    public Post savePost(Post post) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + post.getUser().getUsername() + " not found"));

        post.setUser(user);

        return postRepo.save(post);

    }

    @Override
    public void deletePost(UUID postId) {

    }
}
