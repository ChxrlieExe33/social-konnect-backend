package com.cdcrane.social_konnect_backend.users;

import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        return new User(u.getUsername(), u.getPassword(),
                u.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList()));

    }

    @Override
    public List<ApplicationUser> getAllUsers(){

        // THIS WILL FAIL currently, need to lazily load roles or map to DTO.
        return userRepository.findAll();

    }

    /**
     * Method to get all users from the database using pagination.
     * @param pageable Pageable object from HTTP request, gives information on pages.
     * @return A page of users.
     */
    @Override
    public Page<ApplicationUser> getAllUsersPaginated(Pageable pageable){

        // Have to use this method to get with roles and paginated, map to DTO later.
        return userRepository.findAllWithRolesPaginated(pageable);

    }

}
