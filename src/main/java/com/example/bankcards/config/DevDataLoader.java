package com.example.bankcards.config;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * В профиле dev гарантирует, что пользователи admin и user существуют и пароль "password"
 * закодирован текущим PasswordEncoder (BCrypt). Устраняет 401 из-за несовпадения хеша в сиде.
 */
@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class DevDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEV_PASSWORD = "password";

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureDevUsers() {
        ensureUser("admin", Role.ADMIN);
        ensureUser("user", Role.USER);
    }

    private void ensureUser(String username, Role role) {
        String encodedPassword = passwordEncoder.encode(DEV_PASSWORD);
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.setPasswordHash(encodedPassword);
                    userRepository.save(user);
                    log.info("Dev user '{}' password updated to match encoder", username);
                },
                () -> {
                    User user = User.builder()
                            .username(username)
                            .passwordHash(encodedPassword)
                            .role(role)
                            .createdAt(Instant.now())
                            .build();
                    userRepository.save(user);
                    log.info("Dev user '{}' created with password '{}'", username, DEV_PASSWORD);
                }
        );
    }
}
