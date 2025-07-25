package com.cdcrane.social_konnect_backend.authentication.password_reset;

import java.util.UUID;

public interface PasswordResetUseCase {

    UUID sendPasswordResetEmail(String username);

    void checkResetCode(UUID resetId, int resetCode);

    void resetPassword(UUID resetId, String newPassword);

}
