package com.cdcrane.social_konnect_backend.users.dto;

import com.cdcrane.social_konnect_backend.roles.Role;

import java.util.List;

public record UserWithRolesDTO(long id, String username, String email, String bio, List<Role> roles) {
}
