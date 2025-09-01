package com.cdcrane.social_konnect_backend.config.exceptionhandlers;

import com.cdcrane.social_konnect_backend.authentication.exception.InvalidVerificationCodeException;
import com.cdcrane.social_konnect_backend.authentication.password_reset.exceptions.ResetInvalidException;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.FileTypeNotValidException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.exceptions.UsernameNotValidException;
import com.cdcrane.social_konnect_backend.config.responses.ExceptionErrorResponse;
import com.cdcrane.social_konnect_backend.config.responses.ValidationErrorResponse;
import com.cdcrane.social_konnect_backend.follows.CannotFollowException;
import com.cdcrane.social_konnect_backend.likes.exceptions.UnableToLikeException;
import com.cdcrane.social_konnect_backend.roles.exceptions.RoleNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UnableToChangePasswordException;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UsernameTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
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
                .message("Unknown error occurred. Please contact the developer for assistance.")
                .responseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(System.currentTimeMillis())
                .build();

        log.error("Generic error triggered: {}", ex.toString());

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

        log.warn("Username taken exception triggered: {}", ex.toString());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle validation errors from DTOs passed in the RequestBody of a controller method.
     * @param ex Exception thrown. This exception contains a collection of errors, each being any validation errors triggered.
     * @return Response explaining problem.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Map list of validation errors found, provided by exception, into a hashmap.
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Special error response for validation errors since it needs to be a JSON object for the client to use.
        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .errors(errors)
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    /**
     * Handle errors where the specified user account was not found.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    /**
     * Handle errors where the specified Role was not found.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleRoleNotFoundException(RoleNotFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    /**
     * Handle errors where the user tries to upload a non-permitted file type.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(FileTypeNotValidException.class)
    public ResponseEntity<ExceptionErrorResponse> handleFileTypeNotValidException(FileTypeNotValidException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        log.warn("File type not valid exception triggered: {}", ex.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    /**
     * Handle errors where a user does not have permission to perform an action on a domain object.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(ActionNotPermittedException.class)
    public ResponseEntity<ExceptionErrorResponse> handleActionNotPermittedException(ActionNotPermittedException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.FORBIDDEN.value())
                .timestamp(System.currentTimeMillis())
                .build();

        log.warn("Action not permitted exception triggered: {}", ex.toString());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle errors where a domain object was not found.
     * @param ex Exception thrown.
     * @return Response explaining problem.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.NOT_FOUND.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UsernameNotValidException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUsernameNotValidException(UsernameNotValidException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnableToLikeException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUnableToLikeException(UnableToLikeException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.CONFLICT.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ExceptionErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    /**
     * Handle bad password reset attempts.
     * @param ex The password reset failed exception.
     * @return A response entity with the error.
     */
    @ExceptionHandler(ResetInvalidException.class)
    public ResponseEntity<ExceptionErrorResponse> handleResetInvalidException(ResetInvalidException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnableToChangePasswordException.class)
    public ResponseEntity<ExceptionErrorResponse> handleUnableToChangePasswordException(UnableToChangePasswordException ex) {

        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.CONFLICT.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CannotFollowException.class)
    public ResponseEntity<ExceptionErrorResponse> handleCannotFollowException(CannotFollowException ex) {
        ExceptionErrorResponse error = ExceptionErrorResponse.builder()
                .message(ex.getMessage())
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
