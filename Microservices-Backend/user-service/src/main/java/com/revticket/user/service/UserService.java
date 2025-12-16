package com.revticket.user.service;

import com.revticket.user.dto.PasswordChangeRequest;
import com.revticket.user.dto.UserDto;
import com.revticket.user.entity.User;
import com.revticket.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto getUserProfile(String userId) {
        User user = userRepository.findById(Objects.requireNonNullElse(userId, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    public UserDto updateProfile(String userId, UserDto userDto) {
        User user = userRepository.findById(Objects.requireNonNullElse(userId, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setGender(userDto.getGender());
        user.setAddress(userDto.getAddress());
        user.setPreferredLanguage(userDto.getPreferredLanguage());
        user.setEmailNotifications(userDto.getEmailNotifications());
        user.setSmsNotifications(userDto.getSmsNotifications());
        user.setPushNotifications(userDto.getPushNotifications());

        user = userRepository.save(user);
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void changePassword(String userId, PasswordChangeRequest request) {
        User user = userRepository.findById(Objects.requireNonNullElse(userId, ""))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Helper method to safely return non-null Strings.
     */
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
