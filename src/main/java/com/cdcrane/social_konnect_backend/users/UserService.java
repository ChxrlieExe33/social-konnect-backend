package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.authentication.events.VerificationCodeCreatedEvent;
import com.cdcrane.social_konnect_backend.authentication.exception.InvalidVerificationCodeException;
import com.cdcrane.social_konnect_backend.config.SecurityUtils;
import com.cdcrane.social_konnect_backend.config.exceptions.ActionNotPermittedException;
import com.cdcrane.social_konnect_backend.config.exceptions.ResourceNotFoundException;
import com.cdcrane.social_konnect_backend.config.exceptions.UsernameNotValidException;
import com.cdcrane.social_konnect_backend.config.file_handling.FileHandler;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
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
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepo;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;
    private final FileHandler fileHandler;
    private final PostUseCase postUseCase;
    private final FollowUseCase followUseCase;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder, RoleRepository roleRepo, SecurityUtils securityUtils, ApplicationEventPublisher eventPublisher, FileHandler fileHandler, PostUseCase postUseCase, FollowUseCase followUseCase) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.roleRepo = roleRepo;
        this.securityUtils = securityUtils;
        this.eventPublisher = eventPublisher;
        this.fileHandler = fileHandler;
        this.postUseCase = postUseCase;
        this.followUseCase = followUseCase;
    }

    @Override
    public boolean checkIfUsernameExists(String username){

        return userRepository.existsByUsername(username);

    }

    /**
     * Method to handle registration of new users. Includes an option to change the enabled status.
     * @param registration RegistrationDTO containing basic user information.
     * @param enabled The status you need the “enabled” field to be set to.
     * @return A newly persisted ApplicationUser object.
     */
    @Override
    @Transactional
    public ApplicationUser registerUser(RegistrationDTO registration, boolean enabled){

        boolean alreadyExists = userRepository.existsByUsername(registration.username());

        if (alreadyExists){
            throw new UsernameTakenException("Username " + registration.username() + " is already taken." + " Please choose a different username.");
        }

        if(!TextInputValidator.isValidUsername(registration.username())){
            throw new UsernameNotValidException("Usernames cannot contain HTML tags, please try again.");
        }

        String encodedPassword = encoder.encode(registration.password());

        // Retrieve user role
        Role userRole = roleRepo.findByName("user")
                .orElseThrow(() -> new RoleNotFoundException("The 'user' role was not found"));

        ApplicationUser user = ApplicationUser.builder()
                .username(registration.username())
                .password(encodedPassword)
                .email(registration.email())
                .roles(List.of(userRole))
                .enabled(enabled)
                .verificationCode(securityUtils.generateVerificationCode())
                .build();

        eventPublisher.publishEvent(new VerificationCodeCreatedEvent(user.getEmail(), user.getVerificationCode()));

        return userRepository.save(user);

    }

    /**
     * Method to check a user's verification code, for email verification.
     * @param username The username.
     * @param verificationCode The verification code to check if it is valid.
     * @return An ApplicationUser with the enabled status set to true.
     */
    @Override
    public ApplicationUser checkVerificationCode(String username, int verificationCode) {

        ApplicationUser user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        if (user.isEnabled()) {

            throw new ActionNotPermittedException("User " + username + " is already enabled, no need to verify.");
        }

        if (verificationCode != user.getVerificationCode()) {

            int newCode = securityUtils.generateVerificationCode();

            user.setVerificationCode(newCode);
            userRepository.save(user);

            eventPublisher.publishEvent(new VerificationCodeCreatedEvent(user.getEmail(), newCode));

            throw new InvalidVerificationCodeException("Invalid verification code, please try again. A new code has been sent to " + user.getEmail() + ".");

        }

        user.setEnabled(true);
        return userRepository.save(user);

    }

    /**
     * Get a page of users, searching by a username.
     * @param username The username to search by.
     * @param pageable The pagination data from the query params.
     * @return The page of users.
     */
    @Override
    public Page<ApplicationUser> searchUsersByUsername(String username, Pageable pageable){

        Page<ApplicationUser> users = userRepository.findByUsernameContainingIgnoreCase(username, pageable);

        if(users.isEmpty()){

            throw new UserNotFoundException("No users were found");

        }

        return users;
    }

    /**
     * Get the metadata of a user profile, including the post-count, follower count, the following count and if the current user follows the target user.
     * @param username The username of the target profile.
     * @return The metadata of the user profile.
     */
    @Override
    public UserMetadataDTO getUserMetadataByUsername(String username) {

        ApplicationUser currentUser = securityUtils.getCurrentAuth();

        ApplicationUser targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        int postCount = postUseCase.getPostCountByUserId(targetUser.getId());
        int followerCount = followUseCase.getFollowerCountByUserId(targetUser.getId());
        int followingCount = followUseCase.getFollowingCountByUserId(targetUser.getId());
        boolean currentUserFollowsTarget = followUseCase.existsByFollowerAndFollowed(currentUser.getId(), targetUser.getId());

        return new UserMetadataDTO(followerCount, followingCount, postCount, currentUserFollowsTarget);

    }

    /**
     * Get the metadata of the current user profile, including the post-count, follower count and the following count.
     * @return The metadata of the current user.
     */
    @Override
    public UserMetadataDTO getCurrentUserMetadata() {

        ApplicationUser currentUser = securityUtils.getCurrentAuth();

        int postCount = postUseCase.getPostCountByUserId(currentUser.getId());
        int followerCount = followUseCase.getFollowerCountByUserId(currentUser.getId());
        int followingCount = followUseCase.getFollowingCountByUserId(currentUser.getId());

        return new UserMetadataDTO(followerCount, followingCount, postCount, null);

    }

    /**
     * Method implemented from UserDetailsService from Spring Security for getting a User for authentication/authorization.
     * @param username Username passed for auth
     * @return Spring Security User object used in auth
     * @throws UsernameNotFoundException If the username does not exist or is not found
     */
    @Override
    @Transactional // Has to be transactional to keep the Hibernate session open for lazy loading.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        ApplicationUser u = userRepository.findByUsernameOrEmail(username).orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found"));

        // Initialize roles since they are lazily loaded
        Hibernate.initialize(u.getRoles());

        return new User(u.getUsername(), u.getPassword(), u.isEnabled(), true, true, true,
                u.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList()));

    }

    /**
     * Retrieve an ApplicationUser object complete with roles.
     * @param username Username of the desired user.
     * @return ApplicationUser object with roles.
     */
    @Override
    @Transactional
    public ApplicationUser getUserByUsernameWithRoles(String username) {

        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        // Since roles are lazy loaded.
        Hibernate.initialize(user.getRoles());

        return user;

    }

    /**
     * Retrieve an ApplicationUser object but only with non-related data. (No roles/posts/etc.)
     * @param username Username of the desired user.
     * @return ApplicationUser object containing only details.
     */
    @Override
    public ApplicationUser getUserByUsernameOnlyUserSummary(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

    }

    /**
     * Retrieve all ApplicationUser objects, will not load any relationships.
     * @return List of ApplicationUser objects with non-relation details.
     */
    @Override
    public List<ApplicationUser> getAllUsers(){

        // Will not get roles
        List<ApplicationUser> users = userRepository.findAll();

        if(users.isEmpty()){

            throw new UserNotFoundException("No users were found");

        }

        return users;

    }

    /**
     * Get the profile picture of the username indicated.
     * @param username The username to get the profile picture of.
     * @return The profile picture URL.
     */
    @Override
    public String getProfilePictureUrlByUsername(String username) {

        String url = userRepository.getProfilePictureUrlByUsername(username);

        if (url == null) {
            throw new ResourceNotFoundException("Profile picture for user with username " + username + " not found, cannot get profile picture url.");
        }

        return url;

    }

    /**
     * Change basic profile data including the BIO and profile picture.
     * @param dto The DTO with the updated data.
     * @return The updated user object.
     */
    @Override
    public ApplicationUser changeProfileData(ChangeBioAndPfpDTO dto){

        ApplicationUser user = securityUtils.getCurrentAuth();

        if(dto.pfp() != null) {

            String pfpUrl = fileHandler.saveNewProfilePicture(dto.pfp());

            user.setProfilePictureUrl(pfpUrl);
            user.setBio(dto.bio());

            return userRepository.save(user);

        } else {

            user.setBio(dto.bio());

            return userRepository.save(user);
        }

    }

    /**
     * Update the username of an existing User only if the currently authenticated user is the same user.
     * @param newName The new name to assign to the User.
     * @return An updated ApplicationUser object.
     */
    @Override
    @Transactional
    public ApplicationUser updateUserName(String newName) {

        ApplicationUser auth = securityUtils.getCurrentAuth();

        auth.setUsername(newName);

        return userRepository.save(auth);

    }

    /**
     * Update the password of the currently authenticated user, does not require email verification as the user is actually logged in.
     * @param oldPassword The previous password
     * @param newPassword The new password.
     */
    @Override
    @Transactional
    public void changePassword(String oldPassword ,String newPassword) {

        ApplicationUser user = securityUtils.getCurrentAuth();

        if(!encoder.matches(oldPassword, user.getPassword())){

            throw new UnableToChangePasswordException("Old password is incorrect.");
        }

        if(encoder.matches(newPassword, user.getPassword())){

            throw new UnableToChangePasswordException("New password cannot be the same as the current password.");
        }

        user.setPassword(encoder.encode(newPassword));

        userRepository.save(user);

    }

}
