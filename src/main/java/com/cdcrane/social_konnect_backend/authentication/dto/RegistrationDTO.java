package com.cdcrane.social_konnect_backend.authentication.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationDTO(@NotNull String username, @NotNull String password, @NotNull String email) {
}
