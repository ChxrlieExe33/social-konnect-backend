package com.cdcrane.social_konnect_backend.authentication.dto;

public record RegistrationResponseDTO(long id, String username, String email, boolean enabledStatus) {
}
