package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.authentication.dto.RegistrationDTO;
import com.cdcrane.social_konnect_backend.config.exceptions.UsernameNotValidException;
import com.cdcrane.social_konnect_backend.config.validation.TextInputValidator;
import com.cdcrane.social_konnect_backend.roles.Role;
import com.cdcrane.social_konnect_backend.roles.RoleRepository;
import com.cdcrane.social_konnect_backend.roles.exceptions.RoleNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UserNotFoundException;
import com.cdcrane.social_konnect_backend.users.exceptions.UsernameTakenException;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder, RoleRepository roleRepo) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.roleRepo = roleRepo;
    }

    /**
     * Method to handle registration of new users. Includes an option to change the enabled status.
     * @param registration RegistrationDTO containing basic user information.
     * @param enabled The status you need the “enabled” field to be set to.
     * @return A newly persisted ApplicationUser object.
     */
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
                .build();

        return userRepository.save(user);

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

        ApplicationUser u = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found"));

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


}
