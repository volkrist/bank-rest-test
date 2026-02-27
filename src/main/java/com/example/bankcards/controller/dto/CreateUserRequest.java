package com.example.bankcards.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {
    @NotBlank
    public String username;

    @NotBlank
    public String password;

    /** "USER" или "ADMIN" */
    @NotBlank
    public String role;
}
