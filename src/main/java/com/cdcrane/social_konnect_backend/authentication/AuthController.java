package com.cdcrane.social_konnect_backend.authentication;

import com.cdcrane.social_konnect_backend.authentication.dto.*;
import com.cdcrane.social_konnect_backend.authentication.password_reset.PasswordResetUseCase;
import com.cdcrane.social_konnect_backend.authentication.password_reset.dto.PasswordResetFinalResponse;
import com.cdcrane.social_konnect_backend.authentication.password_reset.dto.PasswordResetRequestResponseDTO;
import com.cdcrane.social_konnect_backend.authentication.password_reset.dto.SubmitNewPasswordDTO;
import com.cdcrane.social_konnect_backend.authentication.password_reset.dto.SubmitResetCodeDTO;
import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.cdcrane.social_konnect_backend.users.UserUseCase;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserUseCase userService;
    private final PasswordResetUseCase passwordResetUseCase;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserUseCase userService, PasswordResetUseCase passwordResetUseCase) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordResetUseCase = passwordResetUseCase;
    }

    /**
     * For registering new users, on successful authentication, provides user information and a JWT.
     * @param registerDTO RegistrationDTO containing basic user information.
     * @param request Request sent.
     * @return A registration response containing user information and an authorization header.
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> register(@Valid @RequestBody RegistrationDTO registerDTO, HttpServletRequest request){

        ApplicationUser user = userService.registerUser(registerDTO, false);

        return ResponseEntity.ok().body(new RegistrationResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled()));

    }

    @PostMapping("/verify")
    public ResponseEntity<RegistrationResponseDTO> verify(@Valid @RequestBody VerifyEmailDTO verifyDTO, HttpServletRequest request){

        ApplicationUser user = userService.checkVerificationCode(verifyDTO.username(), verifyDTO.verificationCode());

        RegistrationResponseDTO response = new RegistrationResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled());

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(java.util.stream.Collectors.toList()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtil.createNewJwt(auth);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        return ResponseEntity.ok().headers(headers).body(response);

    }


    /**
     * For authenticating existing users and providing a JWT in the Authorization header.
     * @param loginDTO User details for a login.
     * @param request The request sent.
     * @return Response object stating the status of the authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO, HttpServletRequest request){

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

    // ------------------------- PASSWORD RESET FUNCTIONALITY -------------------------

    @PostMapping("/resetpassword/{username}")
    public ResponseEntity<PasswordResetRequestResponseDTO> requestPasswordReset(@PathVariable String username){

        UUID resetId = passwordResetUseCase.sendPasswordResetEmail(username);

        return ResponseEntity.ok(new PasswordResetRequestResponseDTO(resetId, "Password reset email sent."));

    }

    @PostMapping("/resetpassword/verify")
    public ResponseEntity<PasswordResetRequestResponseDTO> checkResetCode(@RequestBody SubmitResetCodeDTO submitResetCodeDTO){

        passwordResetUseCase.checkResetCode(submitResetCodeDTO.resetId(), submitResetCodeDTO.resetCode());

        return ResponseEntity.ok(new PasswordResetRequestResponseDTO(submitResetCodeDTO.resetId(), "Password reset code verified. Send new password to /auth/resetpassword/submitnew"));

    }

    @PostMapping("/resetpassword/submitnew")
    public ResponseEntity<PasswordResetFinalResponse> submitNewPassword(@RequestBody SubmitNewPasswordDTO submitNewPasswordDTO){

        passwordResetUseCase.resetPassword(submitNewPasswordDTO.resetId(), submitNewPasswordDTO.newPassword());

        return ResponseEntity.ok(new PasswordResetFinalResponse("Password reset completed. You may now login with your new password."));

    }


}
