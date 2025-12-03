package com.example.devSns.service;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.CommentLikes;
import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.PaginatedDto;
import com.example.devSns.dto.comment.CommentCreateDto;
import com.example.devSns.dto.comment.CommentResponseDto;
import com.example.devSns.exception.ForbiddenException;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.CommentRepository;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTests {

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    CommentService commentService;

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("회원과 게시글이 존재하면 댓글을 생성하고 ID를 반환한다")
        void create_success() {
            // given
            Long memberId = 1L;
            Long postId = 100L;
            CommentCreateDto dto = new CommentCreateDto(postId, "comment");

            Member member = mock(Member.class);
            Post post = mock(Post.class);

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            Comment savedComment = mock(Comment.class);
            when(savedComment.getId()).thenReturn(10L);
            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(savedComment);

            // when
            Long id = commentService.create(dto, memberId);

            // then
            assertEquals(10L, id);
            verify(commentRepository, times(1))
                    .save(any(Comment.class));
        }

        @Test
        @DisplayName("회원이 없으면 NotFoundException 발생")
        void create_member_not_found() {
            // given
            Long memberId = 1L;
            Long postId = 100L;
            CommentCreateDto dto = new CommentCreateDto(postId, "comment");

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentService.create(dto, memberId));
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("게시글이 없으면 InvalidRequestException 발생")
        void create_post_not_found() {
            // given
            Long memberId = 1L;
            Long postId = 100L;
            CommentCreateDto dto = new CommentCreateDto(postId, "comment");

            Member member = mock(Member.class);
            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));
            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(InvalidRequestException.class,
                    () -> commentService.create(dto, memberId));
            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findCommentById()")
    class FindCommentByIdTests {

        @Test
        @DisplayName("댓글이 존재하면 CommentResponseDto 로 반환한다")
        void find_success() {
            // given
            Long commentId = 1L;

            Post post = mock(Post.class);
            when(post.getId()).thenReturn(100L);

            Member member = mock(Member.class);
            when(member.getNickname()).thenReturn("tester");

            Comment comment = mock(Comment.class);
            when(comment.getId()).thenReturn(commentId);
            when(comment.getPost()).thenReturn(post);
            when(comment.getMember()).thenReturn(member);
            when(comment.getContent()).thenReturn("hello");
            when(comment.getCommentLikes()).thenReturn(
                    List.of(mock(CommentLikes.class), mock(CommentLikes.class))
            );
            LocalDateTime now = LocalDateTime.now();
            when(comment.getCreatedAt()).thenReturn(now);
            when(comment.getUpdatedAt()).thenReturn(now);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when
            CommentResponseDto dto = commentService.findCommentById(commentId);

            // then
            assertEquals(commentId, dto.id());
            assertEquals(100L, dto.postId());
            assertEquals("hello", dto.content());
            assertEquals("tester", dto.userName());
            assertEquals(2L, dto.likeCount());
        }

        @Test
        @DisplayName("댓글이 없으면 NotFoundException 발생")
        void find_not_found() {
            // given
            Long commentId = 1L;
            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentService.findCommentById(commentId));
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("작성자이면 댓글을 삭제할 수 있다")
        void delete_success() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;

            Comment comment = mock(Comment.class);
            when(comment.checkOwnership(memberId)).thenReturn(true);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when
            commentService.delete(commentId, memberId);

            // then
            verify(commentRepository, times(1)).delete(comment);
        }

        @Test
        @DisplayName("작성자가 아니면 ForbiddenException 발생")
        void delete_forbidden() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;

            Comment comment = mock(Comment.class);
            when(comment.checkOwnership(memberId)).thenReturn(false);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when & then
            assertThrows(ForbiddenException.class,
                    () -> commentService.delete(commentId, memberId));
            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("댓글이 없으면 NotFoundException 발생")
        void delete_not_found() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentService.delete(commentId, memberId));
        }
    }

    @Nested
    @DisplayName("updateContent()")
    class UpdateContentTests {

        @Test
        @DisplayName("유효한 내용 + 작성자이면 댓글 내용을 수정한다")
        void update_success() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;
            String newContent = "updated";

            Post post = mock(Post.class);
            when(post.getId()).thenReturn(100L);
            Member member = mock(Member.class);
            when(member.getNickname()).thenReturn("tester");

            Comment comment = mock(Comment.class);
            when(comment.checkOwnership(memberId)).thenReturn(true);
            when(comment.getId()).thenReturn(commentId);
            when(comment.getPost()).thenReturn(post);
            when(comment.getMember()).thenReturn(member);
            when(comment.getContent()).thenReturn(newContent);
            when(comment.getCommentLikes()).thenReturn(List.of());
            LocalDateTime now = LocalDateTime.now();
            when(comment.getCreatedAt()).thenReturn(now);
            when(comment.getUpdatedAt()).thenReturn(now);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            GenericDataDto<String> dto = new GenericDataDto<>(newContent);

            // when
            CommentResponseDto response =
                    commentService.updateContent(commentId, dto, memberId);

            // then
            assertEquals(newContent, response.content());
            assertEquals("tester", response.userName());
            verify(comment, times(1)).setContent(newContent);
            verify(commentRepository, atLeastOnce()).findById(commentId);
        }

        @Test
        @DisplayName("내용이 null 또는 빈 문자열이면 InvalidRequestException 발생")
        void update_invalid_content() {
            Long commentId = 1L;
            Long memberId = 1L;

            GenericDataDto<String> emptyDto = new GenericDataDto<>("");

            assertThrows(InvalidRequestException.class,
                    () -> commentService.updateContent(commentId, emptyDto, memberId));
        }

        @Test
        @DisplayName("작성자가 아니면 ForbiddenException 발생")
        void update_forbidden() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;
            GenericDataDto<String> dto = new GenericDataDto<>("new");

            Comment comment = mock(Comment.class);
            when(comment.checkOwnership(memberId)).thenReturn(false);

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.of(comment));

            // when & then
            assertThrows(ForbiddenException.class,
                    () -> commentService.updateContent(commentId, dto, memberId));
            verify(comment, never()).setContent(anyString());
        }

        @Test
        @DisplayName("댓글이 없으면 NotFoundException 발생")
        void update_not_found() {
            // given
            Long commentId = 1L;
            Long memberId = 1L;
            GenericDataDto<String> dto = new GenericDataDto<>("new");

            when(commentRepository.findById(commentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> commentService.updateContent(commentId, dto, memberId));
        }
    }



    @Nested
    @DisplayName("findAsSlice(), findByMemberAsSlice()")
    class SliceTests {

        @Test
        @DisplayName("게시글별 댓글 슬라이스 조회는 Repository 에 위임한다")
        void findAsSlice() {
            // given
            Pageable pageable = PageRequest.of(0, 15);
            Long postId = 100L;

            CommentResponseDto dto = new CommentResponseDto(
                    1L, postId, "c", 1L, "tester", 0L, null, null
            );
            Slice<CommentResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(commentRepository.findCommentSliceByPostIdWithLikeCount(pageable, postId))
                    .thenReturn(slice);

            // when
            Slice<CommentResponseDto> result =
                    commentService.findAsSlice(pageable, postId);

            // then
            assertSame(slice, result);
            verify(commentRepository, times(1))
                    .findCommentSliceByPostIdWithLikeCount(pageable, postId);
        }

        @Test
        @DisplayName("회원별 댓글 슬라이스 조회는 Repository 에 위임한다")
        void findByMemberAsSlice() {
            // given
            Pageable pageable = PageRequest.of(0, 15);
            Long memberId = 1L;

            CommentResponseDto dto = new CommentResponseDto(
                    1L, 100L, "c", 1L,"tester", 0L, null, null
            );
            Slice<CommentResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(commentRepository.findCommentSliceByMemberIdWithLikeCount(pageable, memberId))
                    .thenReturn(slice);

            // when
            Slice<CommentResponseDto> result =
                    commentService.findByMemberAsSlice(pageable, memberId);

            // then
            assertSame(slice, result);
            verify(commentRepository, times(1))
                    .findCommentSliceByMemberIdWithLikeCount(pageable, memberId);
        }
    }
}
