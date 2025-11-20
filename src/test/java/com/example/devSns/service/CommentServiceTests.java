package com.example.devSns.service;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.PaginatedDto;
import com.example.devSns.dto.comment.CommentCreateDto;
import com.example.devSns.dto.comment.CommentResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.CommentRepository;
import com.example.devSns.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTests {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

//    @Test
//    @DisplayName("댓글 생성 성공")
//    void create_success() {
//        // given
//        Long postId = 1L;
//        Post post = createDummyPost(postId);
//        CommentCreateDto createDto = new CommentCreateDto(postId, "Test Comment", "commenter");
//        Comment comment = Comment.create("Test Comment", post, "commenter");
//        comment.setId(101L);
//
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
//
//        // when
//        Long commentId = commentService.create(createDto);
//
//        // then
//        assertNotNull(commentId);
//        assertEquals(101L, commentId);
//        verify(postRepository, times(1)).findById(postId);
//        verify(commentRepository, times(1)).save(any(Comment.class));
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 게시글에 댓글 생성 시 InvalidRequestException 발생")
//    void create_throwsInvalidRequestException_whenPostNotFound() {
//        // given
//        Long postId = 99L;
//        CommentCreateDto createDto = new CommentCreateDto(postId, "Test Comment", "commenter");
//        when(postRepository.findById(postId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(InvalidRequestException.class, () -> commentService.create(createDto));
//        verify(commentRepository, never()).save(any(Comment.class));
//    }
//
//    @Test
//    @DisplayName("ID로 댓글 단건 조회 성공")
//    void findOne_success() {
//        // given
//        Long commentId = 101L;
//        Post post = createDummyPost(1L);
//        Comment comment = createDummyComment(commentId, "Test Content", "user", post);
//
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        // when
//        CommentResponseDto responseDto = commentService.findOne(commentId);
//
//        // then
//        assertNotNull(responseDto);
//        assertEquals(commentId, responseDto.id());
//        assertEquals(post.getId(), responseDto.postId());
//        assertEquals("Test Content", responseDto.content());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 ID로 댓글 조회 시 NotFoundException 발생")
//    void findOne_throwsNotFoundException() {
//        // given
//        Long commentId = 999L;
//        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(NotFoundException.class, () -> commentService.findOne(commentId));
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 성공")
//    void delete_success() {
//        // given
//        Long commentId = 101L;
//        Comment comment = createDummyComment(commentId, "Content", "User", createDummyPost(1L));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        // when
//        commentService.delete(commentId);
//
//        // then
//        verify(commentRepository, times(1)).delete(comment);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 댓글 삭제 시 NotFoundException 발생")
//    void delete_throwsNotFoundException() {
//        // given
//        Long commentId = 999L;
//        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(NotFoundException.class, () -> commentService.delete(commentId));
//    }
//
//    @Test
//    @DisplayName("댓글 내용 수정 성공")
//    void updateContent_success() {
//        // given
//        Long commentId = 101L;
//        String newContent = "Updated Comment";
//        GenericDataDto<String> contentDto = new GenericDataDto<>(newContent);
//        Comment comment = createDummyComment(commentId, "Original", "User", createDummyPost(1L));
//
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        // when
//        commentService.updateContent(commentId, contentDto);
//
//        // then
//        assertEquals(newContent, comment.getContent()); // Verify side-effect
//        verify(commentRepository, times(2)).findById(commentId); // updateContent + findOne
//    }
//
//    @Test
//    @DisplayName("댓글 내용 수정 시 내용이 null이면 InvalidRequestException 발생")
//    void updateContent_throwsInvalidRequestException_whenContentIsNull() {
//        // given
//        Long commentId = 101L;
//        GenericDataDto<String> contentDto = new GenericDataDto<>(null);
//
//        // when & then
//        assertThrows(InvalidRequestException.class, () -> commentService.updateContent(commentId, contentDto));
//    }
//
//    @Test
//    @DisplayName("댓글 좋아요 성공")
//    void like_success() {
//        // given
//        Long commentId = 101L;
//        Comment comment = createDummyComment(commentId, "Content", "User", createDummyPost(1L));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        // when
//        commentService.like(commentId);
//
//        // then
//        verify(commentRepository, times(1)).incrementLikeById(commentId);
//    }
//
//    @Test
//    @DisplayName("댓글 페이지네이션 조회 - 첫 페이지")
//    void findAsPaginated_initialPage() {
//        // given
//        Long postId = 1L;
//        Post post = createDummyPost(postId);
//        List<Comment> comments = List.of(
//                createDummyComment(10L, "Comment 10", "User", post),
//                createDummyComment(9L, "Comment 9", "User", post)
//        );
//        when(commentRepository.findTop15ByCreatedAtBeforeAndPostIdOrderByCreatedAtDescPostIdDesc(any(LocalDateTime.class), eq(postId))).thenReturn(comments);
//
//        // when
//        PaginatedDto<List<CommentResponseDto>> result = commentService.findAsPaginated(new GenericDataDto<>(null), postId);
//
//        // then
//        assertEquals(2, result.data().size());
//        assertEquals(9L, result.nextQueryCriteria());
//    }
//
//    @Test
//    @DisplayName("댓글 페이지네이션 조회 - 결과 없음")
//    void findAsPaginated_noResults() {
//        // given
//        Long postId = 1L;
//        when(commentRepository.findTop15ByIdBeforeAndPostIdOrderByIdDesc(anyLong(), eq(postId))).thenReturn(Collections.emptyList());
//
//        // when
//        PaginatedDto<List<CommentResponseDto>> result = commentService.findAsPaginated(new GenericDataDto<>(100L), postId);
//
//        // then
//        assertTrue(result.data().isEmpty());
//        assertNull(result.nextQueryCriteria());
//    }
//
//    private Post createDummyPost(Long id) {
//        Post post = Post.create("Post content", "postUser");
//        post.setId(id);
//        return post;
//    }
//
//    private Comment createDummyComment(Long id, String content, String userName, Post post) {
//        Comment comment = Comment.create(content, post, userName);
//        comment.setId(id);
//        return comment;
//    }
}