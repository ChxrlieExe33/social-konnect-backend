package com.cdcrane.social_konnect_backend.authentication.events;

import lombok.Getter;

@Getter
public class RegisterVerificationCodeCreatedEvent {

    private final String email;
    private final String username;
    private final int verificationCode;

    public RegisterVerificationCodeCreatedEvent(String email, String username, int verificationCode) {
        this.email = email;
        this.username = username;
        this.verificationCode = verificationCode;
    }
}
