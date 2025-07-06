package com.cdcrane.social_konnect_backend.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    private SecretKey secretKey;

    /**
     * To generate the SecretKey once after construction, instead of each time in the methods.
     */
    @PostConstruct
    private void initializeSecretKey() {

        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("JWT secret must be set");
        }

        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    }

    /**
     * Create a new JWT based on the successful authentication object provided by the controller.
     * @param auth Authentication object containing user details.
     * @return The JWT for the user to access restricted endpoints.
     */
    public String createNewJwt(Authentication auth){

        String jwt = Jwts.builder()
                .issuer(jwtIssuer)
                .subject("JWT Token")
                .claim("username", auth.getName())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))) // Only get authority “name” from each, separate with comma
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)) // About 8 hours
                .signWith(secretKey)
                .compact();

        return jwt;
    }

    /**
     * Validates a provided JWT against the secret key and returns the associated claims.
     * @param jwt The JWT token must be without the "Bearer " prefix.
     * @return The Claims object obtained from the JWT.
     */
    public Claims validateJwt(String jwt){

        try {

            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();

            return claims;

        } catch (ExpiredJwtException e) {

            throw new BadCredentialsException("JWT token expired: " + e.getMessage());

        } catch (Exception e) {

            throw new BadCredentialsException("Invalid JWT token");

        }


    }

}
