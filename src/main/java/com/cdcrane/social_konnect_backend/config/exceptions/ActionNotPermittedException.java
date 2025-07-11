package com.cdcrane.social_konnect_backend.config.exceptions;

public class ActionNotPermittedException extends RuntimeException {

    public ActionNotPermittedException (String message) {
        super(message);
    }
}
