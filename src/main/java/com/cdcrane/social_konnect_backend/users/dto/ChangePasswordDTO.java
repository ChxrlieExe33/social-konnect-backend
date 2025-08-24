package com.cdcrane.social_konnect_backend.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(@NotBlank @Size(min = 8, max = 100) @JsonProperty("new_password") String newPassword,
                                @NotBlank @Size(min = 8, max = 100) @JsonProperty("old_password") String oldPassword) {
}
