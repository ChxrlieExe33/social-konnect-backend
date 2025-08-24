package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.users.dto.ChangeBioAndPfpDTO;
import com.cdcrane.social_konnect_backend.users.dto.UserMetadataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserUseCase extends UserDetailsService {

    ApplicationUser registerUser(RegistrationDTO registration, boolean enabled);

    List<ApplicationUser> getAllUsers();

    ApplicationUser getUserByUsernameWithRoles(String username);

    ApplicationUser getUserByUsernameOnlyUserSummary(String username);

    ApplicationUser checkVerificationCode(String username, int verificationCode);

    ApplicationUser updateUserName(String newName);

    UserMetadataDTO getUserMetadataByUsername(String username);

    UserMetadataDTO getCurrentUserMetadata();

    String getProfilePictureUrlByUsername(String username);

    void changePassword(String oldPassword, String newPassword);

    boolean checkIfUsernameExists(String username);

    Page<ApplicationUser> searchUsersByUsername(String username, Pageable pageable);

    ApplicationUser changeProfileData(ChangeBioAndPfpDTO dto);
}
