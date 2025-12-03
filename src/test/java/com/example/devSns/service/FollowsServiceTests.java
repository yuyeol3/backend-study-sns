package com.example.devSns.service;

import com.example.devSns.domain.Follows;
import com.example.devSns.domain.Member;
import com.example.devSns.dto.follow.FollowRequestDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.FollowsRepository;
import com.example.devSns.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowsServiceTests {

    @Mock
    FollowsRepository followsRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    FollowsService followsService;

    @Nested
    @DisplayName("follow()")
    class FollowTests {

        @Test
        @DisplayName("팔로워와 팔로잉 회원이 모두 존재하고 관계가 없으면 Follows를 생성하고 ID를 반환한다")
        void follow_success() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            Member following = mock(Member.class);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.of(following));

            // 관계 없음
            when(followsRepository.findByFollowerAndFollowing(follower, following))
                    .thenReturn(Optional.empty());

            Follows saved = mock(Follows.class);
            when(saved.getId()).thenReturn(100L);
            when(followsRepository.save(any(Follows.class))).thenReturn(saved);

            // when
            Long id = followsService.follow(dto);

            // then
            assertEquals(100L, id);

            ArgumentCaptor<Follows> captor = ArgumentCaptor.forClass(Follows.class);
            verify(followsRepository, times(1)).save(captor.capture());

            Follows follows = captor.getValue();
            assertNotNull(follows);
        }

        @Test
        @DisplayName("팔로워가 존재하지 않으면 NotFoundException 발생")
        void follow_follower_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> followsService.follow(dto));

            verify(followsRepository, never()).save(any());
        }

        @Test
        @DisplayName("팔로잉 대상이 존재하지 않으면 NotFoundException 발생")
        void follow_following_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> followsService.follow(dto));

            verify(followsRepository, never()).save(any());
        }

        @Test
        @DisplayName("팔로잉 대상이 팔로워와 같으면 InvalidRequestException 발생")
        void follow_following_id_same() {
            // given
            Long followerId = 1L;
            Long followingId = 1L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> followsService.follow(dto));

            verify(followsRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 팔로우 관계가 존재하면 InvalidRequestException 발생")
        void follow_already_exists() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            Member following = mock(Member.class);
            Follows existing = mock(Follows.class);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.of(following));
            when(followsRepository.findByFollowerAndFollowing(follower, following))
                    .thenReturn(Optional.of(existing));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> followsService.follow(dto));

            verify(followsRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("unfollow()")
    class UnfollowTests {

        @Test
        @DisplayName("팔로워와 팔로잉 회원 및 관계가 존재하면 언팔로우에 성공하고 Follows를 삭제한다")
        void unfollow_success() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            Member following = mock(Member.class);
            Follows follows = mock(Follows.class);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.of(following));
            when(followsRepository.findByFollowerAndFollowing(follower, following))
                    .thenReturn(Optional.of(follows));

            // when
            followsService.unfollow(dto);

            // then
            verify(followsRepository, times(1)).delete(follows);
        }

        @Test
        @DisplayName("팔로워가 존재하지 않으면 NotFoundException 발생")
        void unfollow_follower_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> followsService.unfollow(dto));

            verify(followsRepository, never()).delete(any());
        }

        @Test
        @DisplayName("팔로잉 대상이 존재하지 않으면 NotFoundException 발생")
        void unfollow_following_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> followsService.unfollow(dto));

            verify(followsRepository, never()).delete(any());
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 NotFoundException 발생")
        void unfollow_relationship_not_found() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRequestDto dto = new FollowRequestDto(followerId, followingId);

            Member follower = mock(Member.class);
            Member following = mock(Member.class);

            when(memberRepository.findById(followerId))
                    .thenReturn(Optional.of(follower));
            when(memberRepository.findById(followingId))
                    .thenReturn(Optional.of(following));
            when(followsRepository.findByFollowerAndFollowing(follower, following))
                    .thenReturn(Optional.empty());   // 관계 없음

            // when & then
            assertThrows(NotFoundException.class,
                    () -> followsService.unfollow(dto));

            verify(followsRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findFollowers(), findFollowing()")
    class FollowRelationTests {

        @Test
        @DisplayName("findFollowers() 는 MemberRepository.findFollowers() 에 위임한다")
        void findFollowers() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 15);

            MemberResponseDto dto = new MemberResponseDto(2L, "follower");
            Slice<MemberResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(followsRepository.findFollowersOf(memberId, pageable))
                    .thenReturn(slice);

            // when
            Slice<MemberResponseDto> result =
                    followsService.findFollowers(pageable, memberId);

            // then
            assertSame(slice, result);
            verify(followsRepository, times(1))
                    .findFollowersOf(memberId, pageable);
        }

        @Test
        @DisplayName("findFollowing() 는 MemberRepository.findFollowings() 에 위임한다")
        void findFollowing() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 15);

            MemberResponseDto dto = new MemberResponseDto(2L, "following");
            Slice<MemberResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(followsRepository.findFollowingsOf(memberId, pageable))
                    .thenReturn(slice);

            // when
            Slice<MemberResponseDto> result =
                    followsService.findFollowing(pageable, memberId);

            // then
            assertSame(slice, result);
            verify(followsRepository, times(1))
                    .findFollowingsOf(memberId, pageable);
        }
    }

}
