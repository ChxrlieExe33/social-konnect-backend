package com.cdcrane.social_konnect_backend.authentication.password_reset;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "password_reset_sessions")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PasswordResetSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "password_reset_session_id")
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "reset_code")
    private int resetCode;

    @Column(name = "accepted_reset")
    private boolean accepted;

}
