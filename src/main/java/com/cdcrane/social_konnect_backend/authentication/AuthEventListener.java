package com.cdcrane.social_konnect_backend.authentication;

import com.cdcrane.social_konnect_backend.authentication.events.RegisterVerificationCodeCreatedEvent;
import com.cdcrane.social_konnect_backend.config.email.EmailUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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

    @Async
    @EventListener
    public void listenForRegisterVerificationCodeCreated(RegisterVerificationCodeCreatedEvent event) {

        emailUseCase.sendSignupVerificationEmail(event.getEmail(), event.getUsername(), event.getVerificationCode());

    }

}
