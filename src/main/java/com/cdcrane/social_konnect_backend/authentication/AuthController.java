package com.cdcrane.social_konnect_backend.authentication;

import com.cdcrane.social_konnect_backend.authentication.dto.LoginDTO;
import com.cdcrane.social_konnect_backend.authentication.dto.LoginResponseDTO;
import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationResponseDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserService;
import com.cdcrane.social_konnect_backend.users.dto.UserSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * For registering new users, on successful authentication, provides user information and a JWT.
     * @param registerDTO RegistrationDTO containing basic user information.
     * @param request Request sent.
     * @return A registration response containing user information and an authorization header.
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> register(@Valid @RequestBody RegistrationDTO registerDTO, HttpServletRequest request){

        ApplicationUser user = userService.registerUser(registerDTO, true);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(java.util.stream.Collectors.toList()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtil.createNewJwt(auth);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        return ResponseEntity.ok().headers(headers).body(new RegistrationResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled()));

    }


    /**
     * For authenticating existing users and providing a JWT in the Authorization header.
     * @param loginDTO User details for a login.
     * @param request The request sent.
     * @return Response object stating the status of the authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request){

        // This constructor sets “authenticated” to false.
        Authentication auth = new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password());

        try {

            Authentication authentication = authenticationManager.authenticate(auth);

            String jwt = jwtUtil.createNewJwt(authentication);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

            return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO("Authentication completed"));

        } catch (AuthenticationException e) {

            return ResponseEntity.status(401).body(new LoginResponseDTO("Authentication failed, reason: " + e.getMessage()));

        }

    }

    @GetMapping("/user")
    public ResponseEntity<UserSummaryDTO> getCurrentUser(HttpServletRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null){
            return ResponseEntity.status(401).body(null);
        }

        ApplicationUser user = userService.getUserByUsernameWithRoles(auth.getName());

        UserSummaryDTO summary = new UserSummaryDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio());

        return ResponseEntity.ok(summary);
    }

}
