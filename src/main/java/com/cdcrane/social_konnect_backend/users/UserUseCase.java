package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserUseCase extends UserDetailsService {

    ApplicationUser registerUser(RegistrationDTO registration, boolean enabled);

    List<ApplicationUser> getAllUsers();

    ApplicationUser getUserByUsernameWithRoles(String username);

    ApplicationUser getUserByUsernameOnlyUserSummary(String username);

    ApplicationUser checkVerificationCode(String username, int verificationCode);

    ApplicationUser updateUserName(String oldName, String newName);

    void changePassword(String newPassword);

    boolean checkIfUsernameExists(String username);
}
