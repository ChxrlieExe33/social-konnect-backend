package com.cdcrane.social_konnect_backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception
    {

        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/api/post/hello", "/error").permitAll() // Permitted or specific routes first.
                .anyRequest().authenticated()); // .anyRequest always goes last.


        http.formLogin(Customizer.withDefaults());

        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomAuthEntryPoint()));
        http.exceptionHandling(ehc -> ehc.authenticationEntryPoint(new CustomAuthEntryPoint()));

        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));

        // Keeps sessions alive, with basic auth this is disabled by default, later requests will stay authed if you pass the session ID.
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
            session.maximumSessions(1);
        });

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
