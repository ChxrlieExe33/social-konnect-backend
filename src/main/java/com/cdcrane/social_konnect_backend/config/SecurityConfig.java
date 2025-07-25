package com.cdcrane.social_konnect_backend.config;

import com.cdcrane.social_konnect_backend.authentication.JWTUtil;
import com.cdcrane.social_konnect_backend.config.exceptionhandlers.CustomAccessDeniedHandler;
import com.cdcrane.social_konnect_backend.config.exceptionhandlers.CustomAuthEntryPoint;
import com.cdcrane.social_konnect_backend.config.filter.JWTTokenValidatorFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;


@Configuration
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final CorsConfig corsConfig;

    public static final String[] PUBLIC_URIS = {
            "/error",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify",
            "/api/auth/resetpassword",
            "/api/auth/resetpassword/verify",
            "/api/auth/resetpassword/submitnew",
    };

    @Autowired
    public SecurityConfig(JWTUtil jwtUtil, CorsConfig corsConfig) {
        this.jwtUtil = jwtUtil;
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception
    {

        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers(PUBLIC_URIS).permitAll() // Permitted or specific routes first.
                .anyRequest().authenticated()); // .anyRequest always goes last.


        http.cors(cors -> cors.configurationSource(corsConfig));

        // Disable form login and http basic for JWT auth
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterAfter(new JWTTokenValidatorFilter(jwtUtil), ExceptionTranslationFilter.class);

        // Exception handling for AuthenticationExceptions and AccessDeniedExceptions.
        http.exceptionHandling(ehc -> ehc.authenticationEntryPoint(new CustomAuthEntryPoint()));
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));

        // No sessions since we are going to use JWTs
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
