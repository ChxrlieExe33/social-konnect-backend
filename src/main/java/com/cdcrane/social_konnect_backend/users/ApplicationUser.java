package com.cdcrane.social_konnect_backend.users;

import com.cdcrane.social_konnect_backend.roles.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(unique = true)
    private String username;

    private String password;

    private boolean enabled;

    private String email;

    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnoreProperties("users")
    private List<Role> roles;

}
