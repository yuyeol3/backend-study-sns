package com.example.devSns.controller;

import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.auth.AuthResponseDto;
import com.example.devSns.dto.auth.LoginDto;
import com.example.devSns.service.AuthService;
import com.example.devSns.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<GenericDataDto<String>> login(@RequestBody @Valid LoginDto loginDto) {
        AuthResponseDto authResponse = authService.login(loginDto);
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(false) // 개발환경
                .sameSite("Strict")
                .path("/auth")
                .maxAge(jwtUtil.getRefreshExpiration())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new GenericDataDto<>(authResponse.accessToken()));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refresh_token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<GenericDataDto<String>> reAuthByRefreshToken(@CookieValue("refresh_token") String refreshToken) {
        GenericDataDto<String> accessToken = authService.reAuth(refreshToken);
        return ResponseEntity.ok().body(accessToken);
    }


}
