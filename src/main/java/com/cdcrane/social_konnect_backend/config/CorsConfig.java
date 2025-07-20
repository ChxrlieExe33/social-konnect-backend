package com.cdcrane.social_konnect_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig implements CorsConfigurationSource {

    @Value("${cors.allowed-origins}")
    public String[] allowedOrigins;

    @Override
    @Bean
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.stream(allowedOrigins).toList());

        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowCredentials(false);
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setMaxAge(3600L);

        return config;
    }

}