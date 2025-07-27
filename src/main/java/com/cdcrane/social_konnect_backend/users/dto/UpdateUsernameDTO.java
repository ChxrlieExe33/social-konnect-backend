package com.cdcrane.social_konnect_backend.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdateUsernameDTO (@JsonProperty("old_name") @NotBlank String oldName, @NotBlank @JsonProperty("new_name") String newName){
}
