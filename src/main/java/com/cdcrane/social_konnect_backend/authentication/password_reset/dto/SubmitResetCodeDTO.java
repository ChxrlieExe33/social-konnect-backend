package com.cdcrane.social_konnect_backend.authentication.password_reset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitResetCodeDTO (@NotNull @JsonProperty("reset_id") UUID resetId, @NotNull @JsonProperty("reset_code") Integer resetCode) {
}
