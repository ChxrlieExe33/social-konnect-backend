package com.cdcrane.social_konnect_backend.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        ApplicationUser u = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found"));

        return new User(u.getUsername(), u.getPassword(),
                u.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority())).collect(Collectors.toList()));

    }


}
