package com.cdcrane.social_konnect_backend.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// @NotBlank means variable cant be null, empty, or just whitespace.
public record RegistrationDTO(@NotBlank @Size(min = 1, max = 50) String username,
                              @NotBlank @Size(min = 8, max = 100) String password,
                              @NotBlank @Size(min = 3, max = 150) @Email String email) {

}
