package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new AppException("Username already exists", HttpStatus.CONFLICT);
        }

        String normalizedRole = role.trim().toUpperCase();
        if (!normalizedRole.equals("USER") && !normalizedRole.equals("ADMIN")) {
            throw new AppException("role must be USER or ADMIN", HttpStatus.BAD_REQUEST);
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(Role.valueOf(normalizedRole));
        return userRepository.save(u);
    }
}
