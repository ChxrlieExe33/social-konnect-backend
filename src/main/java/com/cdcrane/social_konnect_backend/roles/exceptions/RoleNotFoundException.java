package com.cdcrane.social_konnect_backend.roles.exceptions;

public class RoleNotFoundException extends RuntimeException{

    public RoleNotFoundException(String message) {
        super(message);
    }
}
