package com.cdcrane.social_konnect_backend.authentication.password_reset.exceptions;

public class ResetInvalidException extends RuntimeException {

    public ResetInvalidException(String message) {
        super(message);
    }
}
