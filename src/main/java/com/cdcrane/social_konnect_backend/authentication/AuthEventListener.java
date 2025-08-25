package com.cdcrane.social_konnect_backend.authentication;

import com.cdcrane.social_konnect_backend.authentication.events.VerificationCodeCreatedEvent;
import com.cdcrane.social_konnect_backend.config.email.EmailUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableAsync
public class AuthEventListener {

    private final EmailUseCase emailUseCase;

    @Autowired
    public AuthEventListener(EmailUseCase emailUseCase) {
        this.emailUseCase = emailUseCase;
    }

    /**
     * Listener to execute code on successful authentication events (Logins).
     * @param event The event that was caught.
     */
    @EventListener(AuthenticationSuccessEvent.class)
    public void listenForSuccessful(AuthenticationSuccessEvent event) {

        log.info("Authentication successful for user: " + event.getAuthentication().getName());

    }

    /**
     * Listener to execute code on failed authentication events (Logins).
     * @param event The event that was caught.
     */
    @EventListener(AbstractAuthenticationFailureEvent.class)
    public void listenForFailed(AbstractAuthenticationFailureEvent event) {

        log.error("Authentication failed for user: " + event.getAuthentication().getName() + " for reason : " + event.getException().getMessage());

    }

    @Async
    @EventListener(VerificationCodeCreatedEvent.class)
    public void listenForVerificationCodeCreated(VerificationCodeCreatedEvent event) {

        emailUseCase.sendVerificationEmailHtml(event.getEmail(), event.getUsername(), event.getVerificationCode());

    }


}
