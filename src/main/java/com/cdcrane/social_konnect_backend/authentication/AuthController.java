package com.cdcrane.social_konnect_backend.authentication;

import com.cdcrane.social_konnect_backend.authentication.dto.LoginDTO;
import com.cdcrane.social_konnect_backend.config.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * For authenticating existing users and providing them with a JWT for future auth.
     * @param loginDTO User details for a login.
     * @param request The request sent.
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request){

        Authentication auth = new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password());

        try {

            Authentication authentication = authenticationManager.authenticate(auth);

            String jwt = jwtUtil.createNewJwt(authentication);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

            return ResponseEntity.ok().headers(headers).body("Login successful");

        } catch (AuthenticationException e) {

            return ResponseEntity.status(401).body("Login failed");

        }

    }

}
