package com.cdcrane.social_konnect_backend.config.filter;

import jakarta.servlet.ServletException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, java.io.IOException {


        filterChain.doFilter(request, response);

    }

    /**
     * Should return false if the request is directed to the login endpoint, and true if this is not directed to the login endpoint (we can assume there is already a jwt).
     * @param request The request object
     * @return False if there is a JWT present (not login endpoint), True if there is no JWT present (login endpoint).
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(jakarta.servlet.http.HttpServletRequest request) throws jakarta.servlet.ServletException {

        return request.getRequestURI().contains("/api/auth/login");

    }
}
