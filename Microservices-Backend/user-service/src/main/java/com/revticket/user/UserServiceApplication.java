package com.revticket.user;

import com.revticket.user.entity.User;
import com.revticket.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@revticket.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setName("Admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(User.Role.ADMIN);
                admin.setAuthProvider(User.AuthProvider.LOCAL);
                userRepository.save(admin);
                System.out.println("===========================================");
                System.out.println("✓ Admin user created successfully!");
                System.out.println("Email: " + adminEmail);
                System.out.println("Password: Admin@123");
                System.out.println("Role: ADMIN");
                System.out.println("===========================================");
            }
        };
    }
}
