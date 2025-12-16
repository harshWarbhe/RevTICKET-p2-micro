package com.revticket.user.service;

import com.revticket.user.entity.User;
import com.revticket.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Normalize role to handle case-insensitive values from database
        String normalizedRole = normalizeRole(user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // This is the BCrypt encoded password from database
                .authorities(getAuthorities(normalizedRole))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // Ensure role is uppercase and prefixed with ROLE_
        String roleName = role.toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    /**
     * Normalize role to handle case-insensitive role values from database
     * Handles both "ADMIN" and "admin" from database
     */
    private String normalizeRole(User.Role role) {
        if (role == null) {
            return "USER";
        }
        String roleStr = role.name();
        // Handle case-insensitive comparison
        if (roleStr.equalsIgnoreCase("ADMIN")) {
            return "ADMIN";
        }
        return "USER";
    }
}
