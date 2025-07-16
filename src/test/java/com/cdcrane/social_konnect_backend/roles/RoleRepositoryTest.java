package com.cdcrane.social_konnect_backend.roles;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void shouldFindRoleByName(){

        // Given
        Role role = new Role();
        role.setAuthority("ROLE_USER");
        underTest.save(role);

        // When
        Optional<Role> result = underTest.findByName("ROLE_USER");

        // Then
        assertEquals("ROLE_USER", result.get().getAuthority());
        assertThat(result.get().getId()).isNotNull();

    }

    @Test
    void shouldNotFindRoleByName(){

        // Given no matching role

        // When
        Optional<Role> result = underTest.findByName("ROLE_USER");

        // Then
        assertThat(result).isEmpty();

    }
}