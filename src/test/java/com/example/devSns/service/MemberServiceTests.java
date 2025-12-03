package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.dto.member.MemberCreateDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTests {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("이메일이 중복되지 않으면 비밀번호를 해시하여 회원을 생성하고 ID를 반환한다")
        void create_success() {
            // given
            String nickname = "tester";
            String email = "test@example.com";
            String rawPassword = "password123";

            MemberCreateDto dto = new MemberCreateDto(nickname, email, rawPassword);

            when(memberRepository.findByEmail(email))
                    .thenReturn(Optional.empty());

            Member saved = mock(Member.class);
            when(saved.getId()).thenReturn(1L);
            when(memberRepository.save(any(Member.class))).thenReturn(saved);

            // when
            Long id = memberService.create(dto);

            // then
            assertEquals(1L, id);

            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(1)).save(captor.capture());

            Member memberToSave = captor.getValue();
            assertEquals(nickname, memberToSave.getNickname());
            assertEquals(email, memberToSave.getEmail());
            // 비밀번호는 평문이 아니어야 한다 (정확한 값은 모름)
            assertNotEquals(rawPassword, memberToSave.getPassword());
        }

        @Test
        @DisplayName("이메일이 이미 존재하면 InvalidRequestException 발생")
        void create_email_duplicate() {
            // given
            String email = "dup@example.com";
            MemberCreateDto dto =
                    new MemberCreateDto("tester", email, "password123");

            Member existing = mock(Member.class);
            when(memberRepository.findByEmail(email))
                    .thenReturn(Optional.of(existing));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> memberService.create(dto));

            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getOne()")
    class GetOneTests {

        @Test
        @DisplayName("회원이 존재하면 MemberResponseDto 를 반환한다")
        void getOne_success() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(member.getId()).thenReturn(memberId);
            when(member.getNickname()).thenReturn("tester");

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));

            // when
            MemberResponseDto dto = memberService.getOne(memberId);

            // then
            assertEquals(memberId, dto.id());
            assertEquals("tester", dto.nickname());
        }

        @Test
        @DisplayName("회원이 없으면 NotFoundException 발생")
        void getOne_not_found() {
            // given
            Long memberId = 1L;
            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> memberService.getOne(memberId));
        }
    }

    @Nested
    @DisplayName("findByNickname()")
    class FindByNicknameTests {

        @Test
        @DisplayName("닉네임으로 조회 시 Slice<Member> 를 Slice<MemberResponseDto> 로 매핑해서 반환한다")
        void findByNickname_success() {
            // given
            String nickname = "tester";
            Pageable pageable = PageRequest.of(0, 15);

            Member m1 = mock(Member.class);
            Member m2 = mock(Member.class);

            when(m1.getId()).thenReturn(1L);
            when(m1.getNickname()).thenReturn(nickname);

            when(m2.getId()).thenReturn(2L);
            when(m2.getNickname()).thenReturn(nickname + "2");

            Slice<Member> slice =
                    new SliceImpl<>(List.of(m1, m2), pageable, false);

            when(memberRepository.findMembersByNickname(nickname, pageable))
                    .thenReturn(slice);

            // when
            Slice<MemberResponseDto> result =
                    memberService.findByNickname(pageable, nickname);

            // then
            assertEquals(2, result.getNumberOfElements());
            assertEquals(1L, result.getContent().get(0).id());
            assertEquals("tester", result.getContent().get(0).nickname());
            assertEquals(2L, result.getContent().get(1).id());
            assertEquals("tester2", result.getContent().get(1).nickname());
        }
    }

    }
