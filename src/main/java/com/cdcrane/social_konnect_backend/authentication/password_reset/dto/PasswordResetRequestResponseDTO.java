package com.cdcrane.social_konnect_backend.authentication.password_reset.dto;

import java.util.UUID;

public record PasswordResetRequestResponseDTO(UUID resetId, String message) {
}
