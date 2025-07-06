package com.cdcrane.social_konnect_backend.users.exceptions;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String message) {
        super(message);
    }

}
