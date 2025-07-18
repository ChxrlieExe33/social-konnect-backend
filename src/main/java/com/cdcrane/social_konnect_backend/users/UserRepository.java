package com.cdcrane.social_konnect_backend.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByUsername(String username);

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles WHERE u.username = ?1")
    Optional<ApplicationUser> findByUsernameWithRoles(String username);

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles")
    List<ApplicationUser> findAllWithRoles();

    ApplicationUser findById(long id);

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles WHERE u.id = ?1")
    ApplicationUser findByIdWithRoles(long id);

    boolean existsByUsername(String username);

}
