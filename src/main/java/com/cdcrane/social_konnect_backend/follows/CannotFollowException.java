package com.cdcrane.social_konnect_backend.follows;

public class CannotFollowException extends RuntimeException{
    public CannotFollowException(String message) {
        super(message);
    }
}
