package com.cdcrane.social_konnect_backend.posts;

import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
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
    @Transactional
    public Post savePost(Post post) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + post.getUser().getUsername() + " not found"));

        post.setUser(user);

        String cleanCaption = TextInputValidator.removeHtmlTagsAllowBasic(post.getCaption());

        post.setCaption(cleanCaption);

        return postRepo.save(post);

    }

    @Override
    @Transactional
    public void deletePost(UUID postId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot delete post."));

        if(post.getUser().getId() == user.getId()){

            postRepo.deleteById(postId);

        } else {

            throw new ActionNotPermittedException("User " + username + " is not allowed to delete post with id " + postId + " as it does not belong to them. (Only the user who created the post can delete it.)");
        }

    }

    @Override
    @Transactional
    public Post updatePostCaption(UUID postId, String caption) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepo.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        Post post = postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found, cannot update caption."));

        if (post.getUser().getId() != user.getId()) {

            throw new ActionNotPermittedException("User " + username + " is not allowed to update post with id " + postId + " as it does not belong to them. (Only the user who created the post can update it.)");
        }

        String cleanCaption = TextInputValidator.removeHtmlTagsAllowBasic(caption);

        post.setCaption(cleanCaption);

        return postRepo.save(post);

    }
}
