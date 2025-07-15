package com.cdcrane.social_konnect_backend;

import com.cdcrane.social_konnect_backend.authentication.JWTUtil;
import com.cdcrane.social_konnect_backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SocialKonnectBackendApplicationTests {

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private SecurityConfig securityConfig;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;



    @Test
    void contextLoads() {
    }

}
