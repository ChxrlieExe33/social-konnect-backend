package com.cdcrane.social_konnect_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthEventListener {

    @EventListener(AuthenticationSuccessEvent.class)
    public void listenForSuccessful(AuthenticationSuccessEvent event) {

        log.info("Authentication successful for user: " + event.getAuthentication().getName());

    }

    @EventListener(AbstractAuthenticationFailureEvent.class)
    public void listenForFailed(AbstractAuthenticationFailureEvent event) {

        log.error("Authentication failed for user: " + event.getAuthentication().getName() + " for reason : " + event.getException().getMessage());

    }

}
