package com.cristian.backend.model;

import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "users")
@Entity
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = false; // false until email is verified

    @Column(name = "verification_token")
    private String verificationToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Builder.Default
    @Column(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }

    public enum Role {
        USER,
        ADMIN
    }

}




