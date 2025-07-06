package com.cdcrane.social_konnect_backend.config.exceptionhandlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {


    /**
     * Handles any AccessDeniedException (Authorization) thrown in our security logic.
     * @param request The request attempting to be authorized.
     * @param response The response to be returned.
     * @param accessDeniedException The exception thrown.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setHeader("denied-reason", "Authorization failed");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        LocalDateTime now = LocalDateTime.now();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ? accessDeniedException.getMessage() : "Authorization failed";
        String path = request.getRequestURI();

        String json = String.format("{\"timestamp\":\"%s\",\"status\":\"%s\",\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                now, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), message, path);

        response.getWriter().write(json);

    }

}
