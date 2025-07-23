package com.cdcrane.social_konnect_backend.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record UpdateUsernameDTO (@JsonProperty("old_name") String oldName, @JsonProperty("new_name") String newName){
}
