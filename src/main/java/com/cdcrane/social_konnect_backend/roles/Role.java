package com.cdcrane.social_konnect_backend.roles;

import com.cdcrane.social_konnect_backend.users.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String authority;

    @JsonIgnoreProperties("roles")
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<ApplicationUser> users;

}
