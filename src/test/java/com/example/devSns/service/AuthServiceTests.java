package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.RefreshTokens;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.auth.AuthResponseDto;
import com.example.devSns.dto.auth.LoginDto;
import com.example.devSns.dto.member.MemberCreateDto;
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
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    @Captor
    ArgumentCaptor<RefreshTokens> refreshTokensCaptor;

    private Member createMemberWithPassword(String rawPassword) {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        return new Member("tester", "test@example.com", hash);
    }


    @Nested
    @DisplayName("login()")
    class LoginTests {
        @Test
        @DisplayName("login 성공: 비밀번호가 맞으면 access, refresh 토큰을 발급하고 refreshTokens를 저장한다")
        void login_success() {
            // given
            String rawPassword = "password123";
            Member member = createMemberWithPassword(rawPassword);

            ReflectionTestUtils.setField(member, "id", 1L);
            when(memberRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(member));

            // access/refresh 토큰 관련 mock 세팅
            when(jwtUtil.generateAccessToken(anyLong()))
                    .thenReturn("mock-access-token");

            byte[] rawRefreshBytes = "RAW_REFRESH_TOKEN_32BYTES_____123456".getBytes(); // 길이는 딱히 중요 X
            byte[] hashedRefreshBytes = "HASHED_REFRESH_TOKEN_32_____123456".getBytes();

            when(jwtUtil.generateRefreshToken())
                    .thenReturn(rawRefreshBytes);
            when(jwtUtil.hashToken(rawRefreshBytes))
                    .thenReturn(hashedRefreshBytes);
            when(jwtUtil.getRefreshExpiration())
                    .thenReturn(3600L);

            LoginDto dto = new LoginDto("test@example.com", rawPassword);

            // when
            AuthResponseDto result = authService.login(dto);

            // then
            assertThat(result.accessToken()).isEqualTo("mock-access-token");
            assertThat(result.refreshToken()).isNotBlank();

            // refreshToken이 Base64로 인코딩 되었는지 확인
            byte[] decoded = Base64.getDecoder().decode(result.refreshToken());
            assertThat(decoded).isEqualTo(rawRefreshBytes);

            // RefreshTokens 엔티티가 잘 저장됐는지 확인
            verify(refreshTokensRepository).save(refreshTokensCaptor.capture());
            RefreshTokens saved = refreshTokensCaptor.getValue();

            assertThat(saved.getTokenHash()).isEqualTo(hashedRefreshBytes);
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.isValidToken()).isTrue();
            assertThat(saved.getTokenHash()).isNotNull();
        }

        @Test
        @DisplayName("login 실패: 이메일이 없으면 NotFoundException")
        void login_emailNotFound() {
            when(memberRepository.findByEmail("none@example.com"))
                    .thenReturn(Optional.empty());

            LoginDto dto = new LoginDto("none@example.com", "password123");

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("login 실패: 비밀번호가 틀리면 NotFoundException")
        void login_wrongPassword() {
            // given
            Member member = createMemberWithPassword("correctPassword");
            when(memberRepository.findByEmail(member.getEmail()))
                    .thenReturn(Optional.of(member));

            LoginDto dto = new LoginDto(member.getEmail(), "wrongPassword");

            // expect
            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Invalid email or password");
        }

    }


    @Nested
    @DisplayName("reAuth()")
    class ReAuthTests {
        @Test
        @DisplayName("reAuth 성공: 유효한 refresh 토큰이면 새로운 access 토큰을 발급한다")
        void reAuth_success() {
            // given
            Member member = createMemberWithPassword("password123");
            ReflectionTestUtils.setField(member, "id", 1L);
            byte[] rawBytes = "RAW_REFRESH_TOKEN_32BYTES_____123456".getBytes();
            byte[] hashedBytes = "HASHED_REFRESH_TOKEN_32_____123456".getBytes();

            String encoded = Base64.getEncoder().encodeToString(rawBytes);

            RefreshTokens tokenEntity = new RefreshTokens(
                    hashedBytes,
                    LocalDateTime.now().plusSeconds(3600),
                    member
            );

            when(jwtUtil.hashToken(rawBytes)).thenReturn(hashedBytes);
            when(refreshTokensRepository.findById(hashedBytes))
                    .thenReturn(Optional.of(tokenEntity));
            when(jwtUtil.generateAccessToken(member.getId()))
                    .thenReturn("new-access-token");

            // when
            GenericDataDto<String> result = authService.reAuth(encoded);

            // then
            assertThat(result.data()).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("reAuth 실패: DB에 없는 refresh 토큰이면 UnauthorizedException")
        void reAuth_unknownToken() {
            byte[] rawBytes = "SOME_UNKNOWN_RAW_TOKEN___________".getBytes();
            byte[] hashedBytes = "HASHED_UNKNOWN_TOKEN____________".getBytes();
            String encoded = Base64.getEncoder().encodeToString(rawBytes);

            when(jwtUtil.hashToken(rawBytes)).thenReturn(hashedBytes);
            when(refreshTokensRepository.findById(hashedBytes))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.reAuth(encoded))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Unauthorized");
        }

        @Test
        @DisplayName("reAuth 실패: 만료된 토큰이면 UnauthorizedException")
        void reAuth_expiredToken() {
            Member member = createMemberWithPassword("password123");

            byte[] rawBytes = "RAW_EXPIRED_REFRESH_TOKEN________".getBytes();
            byte[] hashedBytes = "HASHED_EXPIRED_REFRESH_TOKEN_____".getBytes();
            String encoded = Base64.getEncoder().encodeToString(rawBytes);

            RefreshTokens expired = new RefreshTokens(
                    hashedBytes,
                    LocalDateTime.now().minusSeconds(10),
                    member
            );

            when(jwtUtil.hashToken(rawBytes)).thenReturn(hashedBytes);
            when(refreshTokensRepository.findById(hashedBytes))
                    .thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> authService.reAuth(encoded))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Unauthorized");
        }

    }

    @Nested
    @DisplayName("logout()")
    class LogoutTests {
        @Test
        @DisplayName("logout: 해당 refresh 토큰의 valid 플래그를 false로 바꾼다")
        void logout_success() {
            Member member = createMemberWithPassword("password123");

            byte[] rawBytes = "RAW_REFRESH_TOKEN_LOGOUT_________".getBytes();
            byte[] hashedBytes = "HASHED_REFRESH_TOKEN_LOGOUT______".getBytes();
            String encoded = Base64.getEncoder().encodeToString(rawBytes);

            RefreshTokens tokenEntity = new RefreshTokens(
                    hashedBytes,
                    LocalDateTime.now().plusSeconds(3600),
                    member
            );

            when(jwtUtil.hashToken(rawBytes)).thenReturn(hashedBytes);
            when(refreshTokensRepository.findById(hashedBytes))
                    .thenReturn(Optional.of(tokenEntity));

            // when
            authService.logout(encoded);

            // then
            assertThat(tokenEntity.isValidToken()).isFalse();

        }

        @Test
        @DisplayName("logout 실패: DB에 없는 토큰이면 IllegalStateException")
        void logout_tokenNotFound() {
            byte[] rawBytes = "RAW_UNKNOWN_TOKEN_FOR_LOGOUT______".getBytes();
            byte[] hashedBytes = "HASHED_UNKNOWN_TOKEN_FOR_LOGOUT__".getBytes();
            String encoded = Base64.getEncoder().encodeToString(rawBytes);

            when(jwtUtil.hashToken(rawBytes)).thenReturn(hashedBytes);
            when(refreshTokensRepository.findById(hashedBytes))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.logout(encoded))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Logout Failed");
        }
    }


}
