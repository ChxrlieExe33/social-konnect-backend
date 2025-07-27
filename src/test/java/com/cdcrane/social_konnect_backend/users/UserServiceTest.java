package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.authentication.events.VerificationCodeCreatedEvent;
import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.UsernameNotValidException;
import com.cdcrane.social_konnect_backend.roles.Role;
import com.cdcrane.social_konnect_backend.roles.RoleRepository;
import com.cdcrane.social_konnect_backend.roles.exceptions.RoleNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UsernameTakenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    ApplicationEventPublisher eventPublisher;

    private UserService underTest;

    @BeforeEach
    void setUp(){

        underTest = new UserService(userRepository, new BCryptPasswordEncoder(), roleRepository, securityUtils, eventPublisher);

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

    @Test
    void shouldGetUserByUsernameWithRoles(){

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
        ApplicationUser result = underTest.getUserByUsernameWithRoles(user.getUsername());

        // Then
        assertThat(result).isNotNull();

        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getRoles().size()).isEqualTo(1);
        assertThat(result.getRoles().getFirst().getAuthority()).isEqualTo("ROLE_USER");

        verify(userRepository).findByUsername(user.getUsername());

    }

    @Test
    void shouldNotGetUserByUsernameWithRoles(){

        // Given no user
        given(userRepository.findByUsername(any()))
                .willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getUserByUsernameWithRoles("user1"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByUsername("user1");
    }

    // No need to test success on this method, as it is just a proxy for a pre-generated JPA query method.
    @Test
    void shouldNotGetUserByUsernameOnlyUserSummary(){

        // Given no users that match
        given(userRepository.findByUsername(any()))
                .willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getUserByUsernameOnlyUserSummary("user1"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldNotGetAllUsers() {

        // Given no users
        given(userRepository.findAll())
                .willReturn(List.of());

        // Then
        assertThatThrownBy(() -> underTest.getAllUsers())
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    void shouldRegisterUser() {

        // Given
        RegistrationDTO dto = new RegistrationDTO("testusername", "test123", "test@test.com");

        given(userRepository.existsByUsername(dto.username()))
                .willReturn(false);

        Role role = new Role();
        role.setAuthority("ROLE_USER");
        role.setId(1L);

        given(roleRepository.findByName("user"))
                .willReturn(Optional.of(role));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // When
        underTest.registerUser(dto, true);

        // Then
        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);

        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();

        verify(eventPublisher).publishEvent(any(VerificationCodeCreatedEvent.class));

        assertThat(result.getUsername()).isEqualTo(dto.username());
        assertThat(result.getEmail()).isEqualTo(dto.email());

        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getRoles().size()).isEqualTo(1);

        assertThat(result.isEnabled()).isTrue();

        assertThat(passwordEncoder.matches(dto.password(), result.getPassword()))
                .as("Encoded password should match the original password")
                .isTrue();

    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {

        // Given
        RegistrationDTO dto = new RegistrationDTO("testusername", "test123", "test@test.com");

        given(userRepository.existsByUsername(dto.username()))
                .willReturn(true);

        // Then
        assertThatThrownBy(() -> underTest.registerUser(dto, true))
                .isInstanceOf(UsernameTakenException.class);

        verify(userRepository, never()).save(any());

    }

    @Test
    void shouldNotRegisterUserWhenRoleDoesntExist() {

        // Given
        RegistrationDTO dto = new RegistrationDTO("testusername", "test123", "test@test.com");

        given(userRepository.existsByUsername(dto.username()))
                .willReturn(false);

        given(roleRepository.findByName("user"))
                .willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.registerUser(dto, true))
                .isInstanceOf(RoleNotFoundException.class);

        verify(userRepository, never()).save(any());

    }

    @Test
    void shouldNotRegisterUserWhenUsernameIsInvalid() {

        // Given
        RegistrationDTO dto = new RegistrationDTO("<h1>This is a bad username</h1>", "test123", "test@test.com");

        given(userRepository.existsByUsername(dto.username()))
                .willReturn(false);

        // Then
        assertThatThrownBy(() -> underTest.registerUser(dto, true))
                .isInstanceOf(UsernameNotValidException.class);

        verify(userRepository, never()).save(any());

    }

}