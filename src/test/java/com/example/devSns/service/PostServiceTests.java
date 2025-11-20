package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.PaginatedDto;
import com.example.devSns.dto.post.PostCreateDto;
import com.example.devSns.dto.post.PostResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.CommentRepository;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTests {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 성공")
    void create_success() {
        // given
        Long memberId = 100L;
        PostCreateDto dto= new PostCreateDto("test content", memberId);

        Member member = new Member("testUser");

        Post savedPost = new Post("test content", member);
        savedPost.setId(1L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // when
        Long resultId = postService.create(dto);

        // then
        assertEquals(savedPost.getId(), resultId); // 결과 확인
        verify(memberRepository).findById(memberId); // 특정 메서드가 호출되었는지 검증
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("ID로 게시글 단건 조회 성공")
    void findOne_success() {
        // given
        Long postId = 1L;
        Post post = createDummyPost(postId, "Test Content", "testUser");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        PostResponseDto responseDto = postService.findOne(postId);

        // then
        assertNotNull(responseDto);
        assertEquals(postId, responseDto.id());
        assertEquals("Test Content", responseDto.content());
        assertEquals("testUser", responseDto.userName());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 게시글 조회 시 NotFoundException 발생")
    void findOne_throwsNotFoundException() {
        // given
        Long postId = 99L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> postService.findOne(postId));
        verify(commentRepository, never()).countCommentsByPostId(anyLong());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_success() {
        // given
        Long postId = 1L;
        Post post = createDummyPost(postId, "Content", "User");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        postService.delete(postId);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 NotFoundException 발생")
    void delete_throwsNotFoundException() {
        // given
        Long postId = 99L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> postService.delete(postId));
        verify(postRepository, never()).delete(any(Post.class));
    }


    @Test
    @DisplayName("게시글 내용 수정 성공")
    void updateContent_success() {
        // given
        Long postId = 1L;
        String newContent = "Updated Content";
        GenericDataDto<String> contentDto = new GenericDataDto<>(newContent);
        Post post = createDummyPost(postId, "Original Content", "testUser");


        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        PostResponseDto responseDto = postService.updateContent(postId, contentDto);

        // then
        assertEquals(newContent, post.getContent()); // Verify side-effect
        assertEquals(newContent, responseDto.content());
        verify(postRepository, times(2)).findById(postId); // findById is called in updateContent and findOne
    }

    @Test
    @DisplayName("게시글 내용 수정 시 내용이 비어있으면 InvalidRequestException 발생")
    void updateContent_throwsInvalidRequestException_whenContentIsEmpty() {
        // given
        Long postId = 1L;
        GenericDataDto<String> contentDto = new GenericDataDto<>("");

        // when & then
        assertThrows(InvalidRequestException.class, () -> postService.updateContent(postId, contentDto));
    }

    @Test
    @DisplayName("게시글 페이지네이션 조회 - 첫 페이지")
    void findAsSlice_initialPage() {
        // given
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "id"));
        List<PostResponseDto> content = List.of(
            new PostResponseDto(2L, "c2", "user", 0L, null, null, 0L),
            new PostResponseDto(1L, "c1", "user", 0L, null, null, 0L)
        );
        Slice<PostResponseDto> slice = new SliceImpl<>(content, pageable, true);

        when(postRepository.findPostSliceWithLikeCountAndCommentCount(pageable)).thenReturn(slice);

        // when
        Slice<PostResponseDto> result = postService.findAsSlice(pageable);

        // then
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2L, result.getContent().get(0).id());
        assertEquals(1L, result.getContent().get(1).id());
        assertTrue(result.hasNext());
        assertEquals(0, result.getNumber());
        assertEquals(15, result.getSize());

        verify(postRepository, times(1)).findPostSliceWithLikeCountAndCommentCount(pageable);
    }

    @Test
    @DisplayName("게시글 페이지네이션 조회 - 다음 페이지")
    void findAsSlice_nextPage() {
        // given
        // page=1, size=2 → 두 번째 페이지
        Pageable pageable = PageRequest.of(
                1,
                2,
                Sort.by(Sort.Direction.DESC, "id")
        );

        // 두 번째 페이지에 들어갈 더미 데이터
        PostResponseDto p1 = new PostResponseDto(
                8L, "content8", "user1", 0L,
                LocalDateTime.now(), LocalDateTime.now(), 0L
        );
        PostResponseDto p2 = new PostResponseDto(
                7L, "content7", "user2", 0L,
                LocalDateTime.now(), LocalDateTime.now(), 0L
        );
        List<PostResponseDto> content = List.of(p1, p2);

        // hasNext = true → 아직 다음 페이지가 더 있다고 가정
        Slice<PostResponseDto> slice = new SliceImpl<>(content, pageable, true);

        when(postRepository.findPostSliceWithLikeCountAndCommentCount(pageable))
                .thenReturn(slice);

        // when
        Slice<PostResponseDto> result = postService.findAsSlice(pageable);

        // then
        // 1) 내용 검증
        assertEquals(2, result.getNumberOfElements());
        assertEquals(8L, result.getContent().get(0).id());
        assertEquals(7L, result.getContent().get(1).id());

        // 2) 페이지 정보 검증
        assertEquals(1, result.getNumber());     // page index
        assertEquals(2, result.getSize());       // page size
        assertTrue(result.hasNext());            // 아직 다음 페이지 있음

        // 3) Repository가 올바른 Pageable로 호출되었는지 검증
        verify(postRepository, times(1))
                .findPostSliceWithLikeCountAndCommentCount(pageable);
    }


    @Test
    @DisplayName("게시글 페이지네이션 조회 - 결과 없음")
    void findAsSlice_noResults() {
        // given
        Pageable pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "id"));
        List<PostResponseDto> content = List.of(
        );
        Slice<PostResponseDto> slice = new SliceImpl<>(content, pageable, false);

        when(postRepository.findPostSliceWithLikeCountAndCommentCount(pageable)).thenReturn(slice);

        // when
        Slice<PostResponseDto> result = postService.findAsSlice(pageable);

        // then
        assertEquals(0, result.getNumberOfElements());
        assertFalse(result.hasNext());
        assertEquals(0, result.getNumber());
        assertEquals(15, result.getSize());

        verify(postRepository, times(1)).findPostSliceWithLikeCountAndCommentCount(pageable);

    }

    private Post createDummyPost(Long id, String content, String userName) {
        Member member = new Member(userName);
        Post post = Post.create(content, member);
        post.setId(id);
        return post;
    }
}