package com.cdcrane.social_konnect_backend.config.filter;

import com.cdcrane.social_konnect_backend.authentication.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTTokenValidatorFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Filter to extract JWT from the Authorization header and validate it using JWTUtil.
     * @param request The request attempting to be authorized.
     * @param response The response to be returned.
     * @param filterChain Parent filter chain.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ")) {

            String authToken = jwt.substring(7);

            Claims claims = jwtUtil.validateJwt(authToken);

            String username = claims.get("username", String.class);
            String authorities = claims.get("authorities", String.class);

            // This constructor automatically sets authenticated to true, so no need to do it manually.
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("User " + username + " authenticated with JWT token.");

        } else {

            throw new BadCredentialsException("Invalid JWT token, must follow 'Bearer <token>' format.");

        }

        filterChain.doFilter(request, response);

    }

    /**
     * Should return false if the request is directed to the login endpoint, and true if this is not directed to the login endpoint (we can assume there is already a jwt).
     * @param request The request object
     * @return False if there is a JWT present (not login endpoint), True if there is no JWT present (login endpoint).
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws jakarta.servlet.ServletException {

        Set<String> excludedPaths = Set.of("/api/auth/login", "/api/auth/register", "/error");

        return excludedPaths.contains(request.getRequestURI());

    }
}
