package com.cdcrane.social_konnect_backend.authentication.password_reset;

import com.cdcrane.social_konnect_backend.authentication.password_reset.exceptions.ResetInvalidException;
import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.email.EmailUseCase;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordResetService implements PasswordResetUseCase {

    private final EmailUseCase emailUseCase;
    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(EmailUseCase emailUseCase, PasswordResetRepository passwordResetRepository, UserRepository userRepository, SecurityUtils securityUtils, PasswordEncoder passwordEncoder) {
        this.emailUseCase = emailUseCase;
        this.passwordResetRepository = passwordResetRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public UUID sendPasswordResetEmail(String username) {

        ApplicationUser user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found. Cannot send password reset email."));

        // Check for previous attempts and reset attempt with code.
        if(passwordResetRepository.existsByUsername(username)) {

            passwordResetRepository.deleteByUsername(username);

        }

        PasswordResetSession reset = PasswordResetSession.builder()
                .username(username)
                .resetCode(securityUtils.generateVerificationCode())
                .accepted(false)
                .build();

        passwordResetRepository.save(reset);

        // Use non-async call since UX is not as important as on sign-up.
        emailUseCase.sendVerificationEmailHtml(user.getEmail(), user.getUsername(), reset.getResetCode());

        return reset.getId();


    }

    @Override
    @Transactional
    public void checkResetCode(UUID resetId, int resetCode) {

        PasswordResetSession reset = passwordResetRepository.findById(resetId)
                .orElseThrow(() -> new ResetInvalidException("Password reset session with id " + resetId + " not found."));

        if (reset.getResetCode() != resetCode) {

            throw new ResetInvalidException("Password reset code is invalid. Please try again or request new code");

        }

        reset.setAccepted(true);

        passwordResetRepository.save(reset);

    }

    @Override
    @Transactional
    public void resetPassword(UUID resetId, String newPassword) {

        PasswordResetSession reset = passwordResetRepository.findById(resetId)
                .orElseThrow(() -> new ResetInvalidException("Password reset session with id " + resetId + " not found."));

        if (!reset.isAccepted()) {
            throw new ResetInvalidException("This endpoint is prohibited until the password reset has been accepted, submit a new request.");
        }

        ApplicationUser user = userRepository.findByUsername(reset.getUsername())
                .orElseThrow(() -> new ResetInvalidException("User with username " + reset.getUsername() + " not found."));

        if(passwordEncoder.matches(newPassword, user.getPassword())){

            throw new ResetInvalidException("New password cannot be the same as the current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        passwordResetRepository.deleteById(resetId);

    }
}
