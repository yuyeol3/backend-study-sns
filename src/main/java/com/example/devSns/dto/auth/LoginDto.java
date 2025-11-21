package com.example.devSns.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginDto(
        @NotNull @Email String email,
        @NotBlank @Size(min = 6, max = 20) String password
) {
}
