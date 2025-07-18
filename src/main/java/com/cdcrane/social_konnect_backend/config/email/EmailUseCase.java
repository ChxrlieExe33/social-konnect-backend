package com.cdcrane.social_konnect_backend.config.email;

public interface EmailUseCase {

    void sendVerificationEmail(String email, int verificationCode);

}
