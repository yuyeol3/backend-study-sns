package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.RefreshTokens;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.auth.AuthResponseDto;
import com.example.devSns.dto.auth.LoginDto;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.exception.UnauthorizedException;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.RefreshTokensRepository;
import com.example.devSns.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

import static java.util.Base64.getDecoder;


@Service
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokensRepository refreshTokensRepository;

    public AuthService(JwtUtil jwtUtil, MemberRepository memberRepository, RefreshTokensRepository refreshTokensRepository) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.refreshTokensRepository = refreshTokensRepository;
    }

    @Transactional
    public AuthResponseDto login(LoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.email())
                .orElseThrow(()->new NotFoundException("Invalid email or password"));

        String hashedPassword = member.getPassword();
        if (BCrypt.checkpw(loginDto.password(), hashedPassword)) {
            String accessToken = jwtUtil.generateAccessToken(member.getId());
            byte[] rawRefreshToken = jwtUtil.generateRefreshToken();
            byte[] hashedRefreshToken = jwtUtil.hashToken(rawRefreshToken);

            RefreshTokens refreshToken = new RefreshTokens(hashedRefreshToken,
                    LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpiration()),
                    member
            );
            refreshTokensRepository.save(refreshToken);

            return new AuthResponseDto(accessToken, Base64.getEncoder().encodeToString(rawRefreshToken));
        }
        else throw new NotFoundException("Invalid email or password");
    }

    @Transactional
    public void logout(String refreshToken) {
        byte[] rawToken =  Base64.getDecoder().decode(refreshToken);
        byte[] hashedToken = jwtUtil.hashToken(rawToken);
        RefreshTokens refreshTokens = refreshTokensRepository.findById(hashedToken)
                .orElseThrow(()->new IllegalStateException("Logout Failed"));

        refreshTokens.disable();
    }

    public GenericDataDto<String> reAuth(String refreshToken) {
        byte[] rawToken =  Base64.getDecoder().decode(refreshToken);
        byte[] hashedToken = jwtUtil.hashToken(rawToken);

        RefreshTokens refreshTokens = refreshTokensRepository.findById(hashedToken)
                .orElseThrow(()->new UnauthorizedException("Unauthorized"));

        if (!refreshTokens.isValidToken()) {
            throw new UnauthorizedException("Unauthorized");
        }

        Member targetMember = refreshTokens.getMember();
        String accessToken = jwtUtil.generateAccessToken(targetMember.getId());

        return new GenericDataDto<>(accessToken);
    }

}
