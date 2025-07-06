package com.cdcrane.social_konnect_backend.config.exceptionhandlers;

import com.cdcrane.social_konnect_backend.config.responses.ExceptionErrorResponse;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UsernameTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // -------------------- GENERIC EXCEPTIONS --------------------

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

    /**
     * Handle servlet level resource not found errors (Accessing non-existing endpoints, for example)
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // -------------------- SPECIFIC EXCEPTIONS --------------------

    /**
     * Handle when a username is already taken during registration.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUsernameTakenException(UsernameTakenException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.CONFLICT.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle validation errors from DTOs being used in controllers.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message("Request body is invalid, please provide all fields")
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

}
