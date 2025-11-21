package com.example.devSns.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberCreateDto(
    @NotBlank String nickname,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 20) String password
) {
}
