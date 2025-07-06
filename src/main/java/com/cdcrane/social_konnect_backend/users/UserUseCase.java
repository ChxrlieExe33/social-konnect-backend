package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserUseCase extends UserDetailsService {

    ApplicationUser registerUser(RegistrationDTO registration);

    List<ApplicationUser> getAllUsers();

    ApplicationUser getUserByUsernameWithRoles(String username);

    ApplicationUser getUserByUsernameOnlyUserSummary(String username);
}
