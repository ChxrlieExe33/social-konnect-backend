package com.cdcrane.social_konnect_backend.config;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    @Autowired
    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApplicationUser getCurrentAuth() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User with username " + username + " not found."));

        return user;

    }

    public int generateVerificationCode() {

        SecureRandom random = new SecureRandom();

        return random.nextInt(900000) + 100000;

    }
}
