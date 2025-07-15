package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.roles.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByIdWithRoles(){

        // Given
        Role role = new Role();
        role.setAuthority("ROLE_USER");

        ApplicationUser user1 = ApplicationUser.builder()
                .username("user1")
                .password("password1")
                .email("user1@test.com")
                .roles(List.of(role))
                .build();

        ApplicationUser saved = userRepository.save(user1);

        // When
        ApplicationUser result = userRepository.findByIdWithRoles(saved.getId());

        // Then
        assertThat(result.getUsername()).isEqualTo("user1");

        assertThat(result.getRoles()).isNotNull();
    }

    @Test
    void shouldNotFindUserByIdWithRoles(){

        // Given no users with the correct id

        // When
        ApplicationUser result = userRepository.findByIdWithRoles(999);

        // Then
        assertThat(result).isNull();
    }
}