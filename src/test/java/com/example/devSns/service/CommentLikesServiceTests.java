package com.example.devSns.service;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.CommentLikes;
import com.example.devSns.domain.Member;
import com.example.devSns.dto.likes.LikesRequestDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.CommentLikesRepository;
import com.example.devSns.repository.CommentRepository;
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
class CommentLikesServiceTests {

    @Mock
    CommentLikesRepository commentLikesRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentLikesService commentLikesService;

    @Nested
    @DisplayName("like()")
    class LikeTests {

        @Test
        @DisplayName("아직 좋아요를 누르지 않은 경우 like에 성공하고 CommentLikes를 저장한다")
        void like_success() {
            // given
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            Member member = mock(Member.class);
            Comment comment = mock(Comment.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));
            when(commentLikesRepository.findByMemberAndComment(member, comment))
                    .thenReturn(Optional.empty());

            // when
            commentLikesService.like(dto);

            // then
            ArgumentCaptor<CommentLikes> captor =
                    ArgumentCaptor.forClass(CommentLikes.class);
            verify(commentLikesRepository, times(1))
                    .save(captor.capture());

            CommentLikes saved = captor.getValue();
            assertNotNull(saved);
            assertEquals(member, saved.getMember());

            verify(commentLikesRepository, times(1))
                    .findByMemberAndComment(member, comment);
        }

        @Test
        @DisplayName("이미 좋아요를 눌렀다면 InvalidRequestException 이 발생한다")
        void like_already_liked() {
            // given
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            Member member = mock(Member.class);
            Comment comment = mock(Comment.class);
            CommentLikes existing = mock(CommentLikes.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));
            when(commentLikesRepository.findByMemberAndComment(member, comment))
                    .thenReturn(Optional.of(existing));

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> commentLikesService.like(dto));

            verify(commentLikesRepository, never()).save(any());
        }

        @Test
        @DisplayName("회원이 없으면 NotFoundException 이 발생한다")
        void like_member_not_found() {
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> commentLikesService.like(dto));
        }

        @Test
        @DisplayName("댓글이 없으면 NotFoundException 이 발생한다")
        void like_comment_not_found() {
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            Member member = mock(Member.class);
            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> commentLikesService.like(dto));
        }
    }

    @Nested
    @DisplayName("unlike()")
    class UnlikeTests {

        @Test
        @DisplayName("좋아요가 되어 있는 경우 unlike에 성공하고 CommentLikes를 삭제한다")
        void unlike_success() {
            // given
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            Member member = mock(Member.class);
            Comment comment = mock(Comment.class);
            CommentLikes existing = mock(CommentLikes.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));
            when(commentLikesRepository.findByMemberAndComment(member, comment))
                    .thenReturn(Optional.of(existing));

            // when
            commentLikesService.unlike(dto);

            // then
            verify(commentLikesRepository, times(1)).delete(existing);
        }

        @Test
        @DisplayName("좋아요가 되어 있지 않으면 NotFoundException 이 발생한다")
        void unlike_not_liked() {
            // given
            Long commentId = 10L;
            Long memberId = 1L;
            LikesRequestDto dto = new LikesRequestDto(commentId, memberId);

            Member member = mock(Member.class);
            Comment comment = mock(Comment.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));
            when(commentLikesRepository.findByMemberAndComment(member, comment))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentLikesService.unlike(dto));

            verify(commentLikesRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findWhoLiked()")
    class FindWhoLikedTests {

        @Test
        @DisplayName("댓글이 존재하면 해당 댓글을 좋아요한 회원 목록을 슬라이스로 반환한다")
        void findWhoLiked_success() {
            // given
            Long commentId = 10L;
            Pageable pageable = PageRequest.of(0, 15);

            Comment comment = mock(Comment.class);
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            MemberResponseDto dto = new MemberResponseDto(1L, "tester");
            Slice<MemberResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(commentLikesRepository.findLikedUsersByComment(comment, pageable))
                    .thenReturn(slice);

            // when
            Slice<MemberResponseDto> result =
                    commentLikesService.findWhoLiked(pageable, commentId);

            // then
            assertSame(slice, result);
            verify(commentRepository, times(1)).findById(commentId);
            verify(commentLikesRepository, times(1))
                    .findLikedUsersByComment(comment, pageable);
        }

        @Test
        @DisplayName("댓글이 없으면 NotFoundException 이 발생한다")
        void findWhoLiked_comment_not_found() {
            // given
            Long commentId = 10L;
            Pageable pageable = PageRequest.of(0, 15);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentLikesService.findWhoLiked(pageable, commentId));
            verify(commentLikesRepository, never())
                    .findLikedUsersByComment(any(), any());
        }
    }
}
