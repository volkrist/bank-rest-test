package com.example.bankcards.controller;

import com.example.bankcards.controller.dto.CreateUserRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "User management (ADMIN only)")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    @Operation(summary = "Create user (ADMIN)")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest req) {
        User user = adminService.createUser(req.username, req.password, req.role);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }
}
