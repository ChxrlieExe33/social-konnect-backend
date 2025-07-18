package com.cdcrane.social_konnect_backend.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyEmailDTO(@NotBlank String username,
                             @NotNull @JsonProperty("verification_code") Integer verificationCode) {
}
