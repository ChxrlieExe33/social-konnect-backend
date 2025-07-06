package com.cdcrane.social_konnect_backend.users.exceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String message) {
        super(message);
    }

}
