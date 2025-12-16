package com.revticket.user.service;

import com.revticket.user.dto.UserDto;
import com.revticket.user.dto.UserStatsResponse;
import com.revticket.user.entity.User;
import com.revticket.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    public UserDto updateUserRole(String id, String role) {
        User user = userRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            user.setRole(User.Role.valueOf(role.toUpperCase()));
            user = userRepository.save(user);
            return convertToDto(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
    }

    public UserDto updateUserStatus(String id, String status) {
        User user = userRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update user status based on the provided status
        if ("ACTIVE".equalsIgnoreCase(status)) {
            user.setIsActive(true);
        } else if ("BLOCKED".equalsIgnoreCase(status)) {
            user.setIsActive(false);
        } else {
            throw new RuntimeException("Invalid status: " + status + ". Must be ACTIVE or BLOCKED");
        }
        
        user = userRepository.save(user);
        return convertToDto(user);
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public List<Map<String, Object>> getUserBookings(String userId) {
        // This will be handled by calling booking-service in the future
        // For now, return empty list
        return new ArrayList<>();
    }

    public UserStatsResponse getUserStats() {
        Long totalUsers = userRepository.count();
        LocalDateTime now = LocalDateTime.now();
        Long usersLast7Days = userRepository.countUsersCreatedAfter(now.minusDays(7));
        Long usersLast30Days = userRepository.countUsersCreatedAfter(now.minusDays(30));
        return new UserStatsResponse(totalUsers, usersLast7Days, usersLast30Days);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                safe(user.getId()),
                safe(user.getEmail()),
                safe(user.getName()),
                safe(user.getRole() != null ? user.getRole().name() : ""),
                safe(user.getPhone()),
                user.getDateOfBirth(),
                safe(user.getGender()),
                safe(user.getAddress()),
                safe(user.getPreferredLanguage()),
                user.getEmailNotifications(),
                user.getSmsNotifications(),
                user.getPushNotifications(),
                user.getCreatedAt(),
                user.getIsActive() != null ? user.getIsActive() : true);
    }
}
