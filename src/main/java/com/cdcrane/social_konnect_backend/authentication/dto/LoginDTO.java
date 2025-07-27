package com.cdcrane.social_konnect_backend.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(@NotBlank String username, @NotBlank String password) {}
