package com.cdcrane.social_konnect_backend.authentication.password_reset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetSession, UUID> {

    PasswordResetSession getByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);
}
