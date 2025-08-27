package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.authentication.events.RegisterVerificationCodeCreatedEvent;
import com.cdcrane.social_konnect_backend.authentication.exception.InvalidVerificationCodeException;
import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.UsernameNotValidException;
import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.follows.FollowUseCase;
import com.cdcrane.social_konnect_backend.posts.PostUseCase;
import com.cdcrane.social_konnect_backend.roles.Role;
import com.cdcrane.social_konnect_backend.roles.RoleRepository;
import com.cdcrane.social_konnect_backend.roles.exceptions.RoleNotFoundException;
import com.cdcrane.social_konnect_backend.users.dto.ChangeBioAndPfpDTO;
import com.cdcrane.social_konnect_backend.users.dto.UserMetadataDTO;
import com.cdcrane.social_konnect_backend.users.exceptions.UnableToChangePasswordException;
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

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    FileHandler fileHandler;

    @Mock
    PostUseCase postUseCase;

    @Mock
    FollowUseCase followUseCase;

    @Mock
    PasswordEncoder passwordEncoder;

    private UserService underTest;

    @BeforeEach
    void setUp(){

        underTest = new UserService(userRepository, passwordEncoder, roleRepository, securityUtils, eventPublisher, fileHandler, postUseCase, followUseCase);

    }

    @Test
    void shouldCheckIfExistsByUsername(){

        // Given
        given(userRepository.existsByUsername("charlie")).willReturn(true);

        // When
        boolean result = underTest.checkIfUsernameExists("charlie");

        // Then
        assertThat(result).isTrue();

    }

    @Test
    void shouldSearchByUsernameAndGetResults(){

        // Given
        ApplicationUser u1 = ApplicationUser.builder().username("test1").build();
        ApplicationUser u2 = ApplicationUser.builder().username("test2").build();
        Page<ApplicationUser> users = new PageImpl<>(List.of(u1, u2));
        given(userRepository.findByUsernameContainingIgnoreCase("t", Pageable.unpaged())).willReturn(users);

        // When
        Page<ApplicationUser> result = underTest.searchUsersByUsername("t", Pageable.unpaged());

        // Then
        assertThat(result).isNotEmpty();

        List<ApplicationUser> resultData = result.stream().toList();

        assertThat(resultData)
                .isNotEmpty()
                .hasSize(2);

    }

    @Test
    void shouldSearchByUsernameAndNotGetResults(){

        // Given repo returns empty page
        Page<ApplicationUser>  users = new PageImpl<>(List.of());
        given(userRepository.findByUsernameContainingIgnoreCase("t", Pageable.unpaged())).willReturn(users);

        // Then
        assertThatThrownBy(() -> underTest.searchUsersByUsername("t", Pageable.unpaged()))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    void shouldCheckVerificationCodeAndEnableUser(){

        // Given
        ApplicationUser user = ApplicationUser.builder().username("test1").verificationCode(123456).enabled(false).build();
        given(userRepository.findByUsernameWithRoles("test1")).willReturn(Optional.of(user));

        Integer providedCode = 123456;

        // When
        underTest.checkVerificationCode("test1",  providedCode);

        // Then
        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getUsername()).isEqualTo("test1");

    }

    @Test
    void shouldCheckVerificationCodeButFailBecauseUserDoesntExist(){

        // Given
        given(userRepository.findByUsernameWithRoles("test1")).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.checkVerificationCode("test1",  123456))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(ApplicationUser.class));

    }

    @Test
    void shouldCheckVerificationCodeButFailBecauseUserIsAlreadyEnabled(){

        // given
        ApplicationUser user = ApplicationUser.builder().username("test1").verificationCode(123456).enabled(true).build();
        given(userRepository.findByUsernameWithRoles("test1")).willReturn(Optional.of(user));

        // Then
        assertThatThrownBy(() -> underTest.checkVerificationCode("test1",  123456))
                .isInstanceOf(ActionNotPermittedException.class);

        verify(userRepository, never()).save(any(ApplicationUser.class));

    }

    @Test
    void shouldCheckVerificationCodeButFailBecauseCodeIsWrong(){

        // Given
        Integer originalCorrectCode = 123456;
        Integer providedCode = 654321;
        ApplicationUser user = ApplicationUser.builder().username("test1").verificationCode(originalCorrectCode).enabled(false).build();
        given(userRepository.findByUsernameWithRoles("test1")).willReturn(Optional.of(user));

        // When
        assertThatThrownBy(() -> underTest.checkVerificationCode("test1",  providedCode))
                .isInstanceOf(InvalidVerificationCodeException.class);

        // Then
        verify(securityUtils).generateVerificationCode();

        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getVerificationCode()).isNotEqualTo(originalCorrectCode);

    }

    @Test
    void shouldGetUserMetadataByUsername(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).username("test1").build();
        ApplicationUser targetUser = ApplicationUser.builder().id(2L).username("test2").build();

        given(securityUtils.getCurrentAuth()).willReturn(currentUser);
        given(userRepository.findByUsername("test2")).willReturn(Optional.of(targetUser));
        given(postUseCase.getPostCountByUserId(2L)).willReturn(1);
        given(followUseCase.getFollowerCountByUserId(2L)).willReturn(2);
        given(followUseCase.getFollowingCountByUserId(2L)).willReturn(3);
        given(followUseCase.existsByFollowerAndFollowed(1L, 2L)).willReturn(true);

        // When
        UserMetadataDTO data = underTest.getUserMetadataByUsername("test2");

        // Then
        assertThat(data.currentUserFollows()).isTrue();
        assertThat(data.postsCount()).isEqualTo(1);
        assertThat(data.followersCount()).isEqualTo(2);
        assertThat(data.followingCount()).isEqualTo(3);


    }

    @Test
    void shouldGetUserMetadataByUsernameButFailBecauseUserDoesntExist(){

        // Given
        given(userRepository.findByUsername("test2")).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.getUserMetadataByUsername("test2"))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    void shouldGetCurrentUserMetadata(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);
        given(postUseCase.getPostCountByUserId(1L)).willReturn(1);
        given(followUseCase.getFollowerCountByUserId(1L)).willReturn(2);
        given(followUseCase.getFollowingCountByUserId(1L)).willReturn(3);

        // When
        UserMetadataDTO data = underTest.getCurrentUserMetadata();

        // Then
        assertThat(data.postsCount()).isEqualTo(1);
        assertThat(data.followersCount()).isEqualTo(2);
        assertThat(data.followingCount()).isEqualTo(3);
        assertThat(data.currentUserFollows()).isNull();

    }

    @Test
    void shouldChangeProfileDataWithPfpChange(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).bio("Old bio").username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);

        MultipartFile newPfp = Mockito.mock(MultipartFile.class);
        String newPfpUrl = "http://localhost:8080/media/newPfp.png";
        given(fileHandler.saveNewProfilePicture(newPfp)).willReturn(newPfpUrl);

        ChangeBioAndPfpDTO dto = new ChangeBioAndPfpDTO("New bio", newPfp);

        // When
        underTest.changeProfileData(dto);

        // Then
        verify(fileHandler).saveNewProfilePicture(newPfp);

        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();

        assertThat(result.getBio()).isEqualTo(dto.bio());
        assertThat(result.getProfilePictureUrl()).isEqualTo(newPfpUrl);

    }

    @Test
    void shouldChangeProfileDataWithOnlyBioChange(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).bio("Old bio").username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);

        ChangeBioAndPfpDTO dto = new ChangeBioAndPfpDTO("New bio", null);

        // When
        underTest.changeProfileData(dto);

        // Then
        verify(fileHandler, never()).saveNewProfilePicture(any(MultipartFile.class));

        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();

        assertThat(result.getBio()).isEqualTo(dto.bio());


    }

    @Test
    void shouldChangePassword(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).password("jndsnfsdnfkjsnf").username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);

        String newPass = "12345678";
        String oldPass = "<PASSWORD>";

        given(passwordEncoder.matches(newPass, currentUser.getPassword())).willReturn(false);

        // When
        underTest.changePassword(oldPass, newPass);

        // Then
        verify(userRepository).save(any());

    }

    @Test
    void shouldNotChangePasswordBecauseItIsTheSame(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).password("jndsnfsdnfkjsnf").username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);

        String newPass = "jndsnfsdnfkjsnf";
        String oldPass = "jndsnfsdnfkjsnf";

        given(passwordEncoder.matches(newPass, currentUser.getPassword())).willReturn(true);

        // Then
        assertThatThrownBy(() -> underTest.changePassword(oldPass, newPass))
                .isInstanceOf(UnableToChangePasswordException.class);

        verify(userRepository, never()).save(any(ApplicationUser.class));
    }

    @Test
    void shouldUpdateUsername(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).username("test1").build();
        given(securityUtils.getCurrentAuth()).willReturn(currentUser);

        String newUsername = "test2";

        // When
        underTest.updateUserName(newUsername);

        // Then
        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();
        assertThat(result.getUsername()).isEqualTo(newUsername);

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

        given(userRepository.findByUsernameOrEmail(user.getUsername()))
                .willReturn(Optional.of(user));

        // When
        UserDetails result = underTest.loadUserByUsername(user.getUsername());

        // Then
        verify(userRepository).findByUsernameOrEmail(user.getUsername()); // Should call userRepository(findByUsername)

        assertThat(result.getAuthorities()).hasSize(1); // Should have exactly one role
        assertThat(result.getUsername()).isEqualTo(user.getUsername()); // Should have the correct username

    }

    @Test
    void shouldNotLoadUserByUsername(){

        // Given
        given(userRepository.findByUsernameOrEmail("user1")).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> underTest.loadUserByUsername("user1"))
                .isInstanceOf(UsernameNotFoundException.class); // Check it throws the correct exception

        verify(userRepository).findByUsernameOrEmail("user1");
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
    void shouldGetAllUsers(){

        // Given
        ApplicationUser currentUser = ApplicationUser.builder().id(1L).username("test1").build();
        ApplicationUser targetUser = ApplicationUser.builder().id(2L).username("test2").build();
        given(userRepository.findAll()).willReturn(List.of(currentUser, targetUser));

        // When
        List<ApplicationUser> result = underTest.getAllUsers();

        // Then
        assertThat(result).hasSize(2);

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

        PasswordEncoder enc = new BCryptPasswordEncoder();

        given(passwordEncoder.encode(dto.password())).willReturn(enc.encode(dto.password()));

        // When
        underTest.registerUser(dto, true);

        // Then
        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);

        verify(userRepository).save(userCaptor.capture());

        ApplicationUser result = userCaptor.getValue();

        verify(eventPublisher).publishEvent(any(RegisterVerificationCodeCreatedEvent.class));

        assertThat(result.getUsername()).isEqualTo(dto.username());
        assertThat(result.getEmail()).isEqualTo(dto.email());

        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getRoles().size()).isEqualTo(1);

        assertThat(result.isEnabled()).isTrue();

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