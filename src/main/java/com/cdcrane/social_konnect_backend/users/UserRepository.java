package com.cdcrane.social_konnect_backend.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByUsername(String username);

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles")
    Page<ApplicationUser> findAllWithRolesPaginated(Pageable pageable);

}
