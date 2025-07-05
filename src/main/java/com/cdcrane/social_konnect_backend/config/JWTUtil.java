package com.cdcrane.social_konnect_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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


    public String createNewJwt(Authentication auth){

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .issuer("Social Konnect")
                .subject("JWT Token")
                .claim("username", auth.getName())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))) // Only get authority “name” from each, separate with comma
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30000000)) // About 8 hours
                .signWith(secretKey)
                .compact();

        return jwt;
    }

    public Claims validateJwt(String jwt){

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        try {

            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();

            return claims;

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token");
        }


    }

}
