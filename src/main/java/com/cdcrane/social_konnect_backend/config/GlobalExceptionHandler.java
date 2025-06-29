package com.cdcrane.social_konnect_backend.config;

import com.cdcrane.social_konnect_backend.config.responses.ExceptionErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This method catches any exceptions not explicitly caught by any other methods in this handler.
     * @param ex The exception thrown.
     * @return Returns a response entity to the user with error details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionErrorResponse> handleException(Exception ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        System.err.println("Generic error triggered: " + ex.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
