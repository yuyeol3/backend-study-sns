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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    MemberRepository memberRepository;

    @Mock
    RefreshTokensRepository refreshTokensRepository;

    @InjectMocks
    AuthService authService;

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("이메일/비밀번호가 올바르면 access/refresh 토큰을 반환한다")
        void login_success() {
            // given
            String email = "test@example.com";
            String rawPassword = "password123";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            LoginDto loginDto = new LoginDto(email, rawPassword);

            Member member = mock(Member.class);
            when(memberRepository.findByEmail(email))
                    .thenReturn(Optional.of(member));
            when(member.getPassword()).thenReturn(hashedPassword);
            when(member.getId()).thenReturn(1L);

            String expectedAccessToken = "access-token";
            byte[] refreshTokenId = new byte[]{1, 2, 3, 4};
            String expectedRefreshToken = Base64.getEncoder().encodeToString(refreshTokenId);

            when(jwtUtil.generateAccessToken(1L)).thenReturn(expectedAccessToken);
            when(jwtUtil.generateRefreshToken()).thenReturn(refreshTokenId);
            when(jwtUtil.getRefreshExpiration()).thenReturn(3600L); // seconds

            // when
            AuthResponseDto response = authService.login(loginDto);

            // then
            assertEquals(expectedAccessToken, response.accessToken());
            assertEquals(expectedRefreshToken, response.refreshToken());

            // refresh token 이 member 에 추가되었는지 확인
            ArgumentCaptor<RefreshTokens> captor = ArgumentCaptor.forClass(RefreshTokens.class);
            verify(member, times(1)).addRefreshToken(captor.capture());
            RefreshTokens savedToken = captor.getValue();
            assertNotNull(savedToken);
            assertEquals(expectedRefreshToken, savedToken.getRefreshToken());

            verify(jwtUtil, times(1)).generateAccessToken(1L);
            verify(jwtUtil, times(1)).generateRefreshToken();
        }

        @Test
        @DisplayName("해당 이메일이 없으면 NotFoundException 발생")
        void login_email_not_found() {
            // given
            String email = "not-exist@example.com";
            LoginDto loginDto = new LoginDto(email, "password123");

            when(memberRepository.findByEmail(email))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class, () -> authService.login(loginDto));
            verify(jwtUtil, never()).generateAccessToken(anyLong());
        }

        @Test
        @DisplayName("비밀번호가 틀리면 NotFoundException 발생 (보안상 동일 메시지)")
        void login_wrong_password() {
            // given
            String email = "test@example.com";
            String rawPassword = "password123";
            String otherPassword = "wrong-password";

            String hashedPassword = BCrypt.hashpw(otherPassword, BCrypt.gensalt());

            LoginDto loginDto = new LoginDto(email, rawPassword);

            Member member = mock(Member.class);
            when(memberRepository.findByEmail(email))
                    .thenReturn(Optional.of(member));
            when(member.getPassword()).thenReturn(hashedPassword);

            // when & then
            assertThrows(NotFoundException.class, () -> authService.login(loginDto));
            verify(jwtUtil, never()).generateAccessToken(anyLong());
        }
    }

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("유효한 refresh token 이면 해당 토큰을 disable 한다")
        void logout_success() {
            // given
            byte[] tokenId = new byte[]{10, 20, 30};
            String encoded = Base64.getEncoder().encodeToString(tokenId);

            RefreshTokens refreshTokens = mock(RefreshTokens.class);
            when(refreshTokensRepository.findById(tokenId))
                    .thenReturn(Optional.of(refreshTokens));

            // when
            authService.logout(encoded);

            // then
            verify(refreshTokensRepository, times(1)).findById(tokenId);
            verify(refreshTokens, times(1)).disable();
        }

        @Test
        @DisplayName("DB에 없는 refresh token 이면 IllegalStateException 발생")
        void logout_token_not_found() {
            // given
            byte[] tokenId = new byte[]{10, 20, 30};
            String encoded = Base64.getEncoder().encodeToString(tokenId);

            when(refreshTokensRepository.findById(tokenId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(IllegalStateException.class, () -> authService.logout(encoded));
        }
    }

    @Nested
    @DisplayName("reAuth()")
    class ReAuthTests {

        @Test
        @DisplayName("유효한 refresh token 이면 새 access token 을 발급한다")
        void reauth_success() {
            // given
            byte[] tokenId = new byte[]{5, 6, 7, 8};
            String encoded = Base64.getEncoder().encodeToString(tokenId);

            RefreshTokens refreshTokens = mock(RefreshTokens.class);
            when(refreshTokensRepository.findById(tokenId))
                    .thenReturn(Optional.of(refreshTokens));
            when(refreshTokens.isValidToken()).thenReturn(true);

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(42L);
            when(refreshTokens.getMember()).thenReturn(member);

            String expectedAccessToken = "new-access-token";
            when(jwtUtil.generateAccessToken(42L)).thenReturn(expectedAccessToken);

            // when
            GenericDataDto<String> dto = authService.reAuth(encoded);

            // then
            assertEquals(expectedAccessToken, dto.data());
            verify(jwtUtil, times(1)).generateAccessToken(42L);
        }

        @Test
        @DisplayName("refresh token 이 DB에 없으면 UnauthorizedException 발생")
        void reauth_token_not_found() {
            // given
            byte[] tokenId = new byte[]{5, 6, 7, 8};
            String encoded = Base64.getEncoder().encodeToString(tokenId);

            when(refreshTokensRepository.findById(tokenId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(UnauthorizedException.class, () -> authService.reAuth(encoded));
            verify(jwtUtil, never()).generateAccessToken(anyLong());
        }

        @Test
        @DisplayName("refresh token 이 만료/비활성화 상태면 UnauthorizedException 발생")
        void reauth_invalid_token() {
            // given
            byte[] tokenId = new byte[]{5, 6, 7, 8};
            String encoded = Base64.getEncoder().encodeToString(tokenId);

            RefreshTokens refreshTokens = mock(RefreshTokens.class);
            when(refreshTokensRepository.findById(tokenId))
                    .thenReturn(Optional.of(refreshTokens));
            when(refreshTokens.isValidToken()).thenReturn(false);

            // when & then
            assertThrows(UnauthorizedException.class, () -> authService.reAuth(encoded));
            verify(jwtUtil, never()).generateAccessToken(anyLong());
        }
    }
}
