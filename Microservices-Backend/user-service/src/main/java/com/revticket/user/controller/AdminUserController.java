package com.revticket.user.controller;

import com.revticket.user.dto.UserDto;
import com.revticket.user.dto.UserStatsResponse;
import com.revticket.user.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        return ResponseEntity.ok(adminUserService.updateUserRole(id, role));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return ResponseEntity.ok(adminUserService.updateUserStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<Map<String, Object>>> getUserBookings(@PathVariable String id) {
        return ResponseEntity.ok(adminUserService.getUserBookings(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        UserStatsResponse stats = adminUserService.getUserStats();
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalUsers", stats.getTotalUsers());
        response.put("usersLast7Days", stats.getUsersLast7Days());
        response.put("usersLast30Days", stats.getUsersLast30Days());
        return ResponseEntity.ok(response);
    }
}
