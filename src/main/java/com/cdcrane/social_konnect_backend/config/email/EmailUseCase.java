package com.cdcrane.social_konnect_backend.config.email;

import java.util.UUID;

public interface EmailUseCase {

    void sendSignupVerificationEmail(String email, String username, int verificationCode);

    void sendForgotPasswordVerificationEmail(String email, String username, int verificationCode, UUID resetSessionId);

}
