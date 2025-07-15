package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.roles.Role;
import com.cdcrane.social_konnect_backend.roles.RoleRepository;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserService underTest;

    @BeforeEach
    void setUp(){

        underTest = new UserService(userRepository, new BCryptPasswordEncoder(), roleRepository);

    }

    @Test
    void shouldLoadUserByUsername(){

        // Given
        Role role = new Role();
        role.setAuthority("ROLE_USER");

        ApplicationUser user = ApplicationUser.builder()
                .username("user1")
                .password("password1")
                .email("email1@test.com")
                .roles(List.of(role))
                .build();

        given(userRepository.findByUsername(user.getUsername()))
                .willReturn(Optional.of(user));

        // When
        UserDetails result = underTest.loadUserByUsername(user.getUsername());

        // Then
        verify(userRepository).findByUsername(user.getUsername()); // Should call userRepository(findByUsername)

        assertThat(result.getAuthorities()).hasSize(1); // Should have exactly one role
        assertThat(result.getUsername()).isEqualTo(user.getUsername()); // Should have the correct username

    }

    @Test
    void shouldNotLoadUserByUsername(){

        // Given
        given(userRepository.findByUsername("user1")).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.loadUserByUsername("user1"))
                .isInstanceOf(UsernameNotFoundException.class); // Check it throws the correct exception


    }

}