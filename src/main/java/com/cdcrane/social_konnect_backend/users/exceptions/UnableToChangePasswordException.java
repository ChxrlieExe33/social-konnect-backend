package com.cdcrane.social_konnect_backend.users.exceptions;

public class UnableToChangePasswordException extends RuntimeException{

    public UnableToChangePasswordException(String message) {
        super(message);
    }
}
