package com.cdcrane.social_konnect_backend.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserUseCase extends UserDetailsService {

    Page<ApplicationUser> getAllUsersPaginated(Pageable pageable);

    List<ApplicationUser> getAllUsers();
}
