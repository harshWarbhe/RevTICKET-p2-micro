package com.revticket.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    private String gender;

    @Column(length = 500)
    private String address;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;

    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId; // Store the OAuth2 provider's user ID

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum Role {
        USER, ADMIN
    }

    public enum AuthProvider {
        LOCAL, // Password-based authentication
        GOOGLE, // Google OAuth2
        FACEBOOK, // Facebook OAuth2
        GITHUB // GitHub OAuth2
    }
}
