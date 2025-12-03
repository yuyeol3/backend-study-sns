package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.domain.PostLikes;
import com.example.devSns.dto.likes.LikesRequestDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostLikesRepository;
import com.example.devSns.repository.PostRepository;
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
class PostLikesServiceTests {

    @Mock
    PostLikesRepository postLikesRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostLikesService postLikesService;

    @Nested
    @DisplayName("like()")
    class LikeTests {

        @Test
        @DisplayName("아직 좋아요를 누르지 않은 경우 like에 성공하고 PostLikes를 저장한다")
        void like_success() {
            // given
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            Member member = mock(Member.class);
            Post post = mock(Post.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            // 아직 좋아요 안 눌렀으므로 Optional.empty()
            when(postLikesRepository.findByMemberAndPost(member, post))
                    .thenReturn(Optional.empty());

            // when
            postLikesService.like(dto);

            // then
            ArgumentCaptor<PostLikes> captor = ArgumentCaptor.forClass(PostLikes.class);
            verify(postLikesRepository, times(1)).save(captor.capture());

            PostLikes saved = captor.getValue();
            assertNotNull(saved);
            assertEquals(member, saved.getMember());
            // post는 PostLikes에 getter가 없어서 여기까지 확인해도 충분

            // findByMemberAndPost는 최소 한 번 호출
            verify(postLikesRepository, times(1))
                    .findByMemberAndPost(member, post);
        }

        @Test
        @DisplayName("이미 좋아요를 눌렀다면 InvalidRequestException 이 발생한다")
        void like_already_liked() {
            // given
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            Member member = mock(Member.class);
            Post post = mock(Post.class);
            PostLikes existing = mock(PostLikes.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));
            when(postLikesRepository.findByMemberAndPost(member, post))
                    .thenReturn(Optional.of(existing));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> postLikesService.like(dto));

            verify(postLikesRepository, never()).save(any());
        }

        @Test
        @DisplayName("회원이 없으면 NotFoundException 이 발생한다")
        void like_member_not_found() {
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> postLikesService.like(dto));
        }

        @Test
        @DisplayName("게시글이 없으면 NotFoundException 이 발생한다")
        void like_post_not_found() {
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            Member member = mock(Member.class);
            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> postLikesService.like(dto));
        }
    }

    @Nested
    @DisplayName("unlike()")
    class UnlikeTests {

        @Test
        @DisplayName("좋아요가 되어 있는 경우 unlike에 성공하고 PostLikes를 삭제한다")
        void unlike_success() {
            // given
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            Member member = mock(Member.class);
            Post post = mock(Post.class);
            PostLikes existing = mock(PostLikes.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));
            when(postLikesRepository.findByMemberAndPost(member, post))
                    .thenReturn(Optional.of(existing));

            // when
            postLikesService.unlike(dto);

            // then
            verify(postLikesRepository, times(1)).delete(existing);
        }

        @Test
        @DisplayName("좋아요가 되어 있지 않으면 NotFoundException 이 발생한다")
        void unlike_not_liked() {
            // given
            Long postId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(postId, memberId);

            Member member = mock(Member.class);
            Post post = mock(Post.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));
            when(postLikesRepository.findByMemberAndPost(member, post))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postLikesService.unlike(dto));

            verify(postLikesRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findWhoLiked()")
    class FindWhoLikedTests {

        @Test
        @DisplayName("게시글이 존재하면 해당 게시글을 좋아요한 회원 목록을 슬라이스로 반환한다")
        void findWhoLiked_success() {
            // given
            Long postId = 10L;
            Pageable pageable = PageRequest.of(0, 15);

            Post post = mock(Post.class);
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            MemberResponseDto dto = new MemberResponseDto(1L, "tester");
            Slice<MemberResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(postLikesRepository.findLikedUsersByPost(post, pageable))
                    .thenReturn(slice);

            // when
            Slice<MemberResponseDto> result =
                    postLikesService.findWhoLiked(pageable, postId);

            // then
            assertSame(slice, result);
            verify(postRepository, times(1)).findById(postId);
            verify(postLikesRepository, times(1))
                    .findLikedUsersByPost(post, pageable);
        }

        @Test
        @DisplayName("게시글이 없으면 NotFoundException 이 발생한다")
        void findWhoLiked_post_not_found() {
            // given
            Long postId = 10L;
            Pageable pageable = PageRequest.of(0, 15);

            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postLikesService.findWhoLiked(pageable, postId));
            verify(postLikesRepository, never())
                    .findLikedUsersByPost(any(), any());
        }
    }
}
