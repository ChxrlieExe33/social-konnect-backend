package com.cdcrane.social_konnect_backend.authentication.dto;

import jakarta.validation.constraints.*;

public record RegistrationDTO(@NotBlank @Pattern(regexp = "^[^\\s]+$", message = "Username must not contain spaces") @Size(min = 1, max = 50) String username,
                              @NotBlank @Size(min = 8, max = 100) String password,
                              @NotBlank @Size(min = 3, max = 150) @Email String email) {

}
