package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.post.PostCreateDto;
import com.example.devSns.dto.post.PostResponseDto;
import com.example.devSns.exception.ForbiddenException;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTests {

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    PostService postService;

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("회원이 존재하면 게시글을 생성하고 ID를 반환한다")
        void create_success() {
            // given
            Long memberId = 1L;
            PostCreateDto dto = new PostCreateDto("hello world");

            Member member = mock(Member.class);
            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.of(member));

            Post savedPost = mock(Post.class);
            when(savedPost.getId()).thenReturn(10L);
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            // when
            Long postId = postService.create(dto, memberId);

            // then
            assertEquals(10L, postId);
            verify(memberRepository, times(1)).findById(memberId);
            verify(postRepository, times(1)).save(any(Post.class));
        }

        @Test
        @DisplayName("회원이 없으면 NotFoundException 발생")
        void create_member_not_found() {
            // given
            Long memberId = 1L;
            PostCreateDto dto = new PostCreateDto("hello world");

            when(memberRepository.findById(memberId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postService.create(dto, memberId));
            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findPostById()")
    class FindPostByIdTests {

        @Test
        @DisplayName("게시글이 존재하면 PostResponseDto 로 반환한다")
        void find_success() {
            // given
            Long postId = 1L;

            Member member = mock(Member.class);
            when(member.getNickname()).thenReturn("tester");

            Post post = new Post("content", member);
            // id, likes, comments가 null이어도 from()은 null 허용

            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            // when
            PostResponseDto dto = postService.findPostById(postId);

            // then
            assertEquals("content", dto.content());
            assertEquals("tester", dto.userName());
            verify(postRepository, times(1)).findById(postId);
        }

        @Test
        @DisplayName("게시글이 없으면 NotFoundException 발생")
        void find_not_found() {
            // given
            Long postId = 1L;
            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postService.findPostById(postId));
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("작성자이면 게시글을 삭제할 수 있다")
        void delete_success() {
            // given
            Long postId = 1L;
            Long memberId = 1L;

            Post post = mock(Post.class);
            when(post.checkOwnership(memberId)).thenReturn(true);
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            // when
            postService.delete(postId, memberId);

            // then
            verify(postRepository, times(1)).delete(post);
        }

        @Test
        @DisplayName("작성자가 아니면 ForbiddenException 발생")
        void delete_forbidden() {
            // given
            Long postId = 1L;
            Long memberId = 1L;

            Post post = mock(Post.class);
            when(post.checkOwnership(memberId)).thenReturn(false);
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            // when & then
            assertThrows(ForbiddenException.class,
                    () -> postService.delete(postId, memberId));
            verify(postRepository, never()).delete(any());
        }

        @Test
        @DisplayName("게시글이 없으면 NotFoundException 발생")
        void delete_not_found() {
            // given
            Long postId = 1L;
            Long memberId = 1L;

            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postService.delete(postId, memberId));
        }
    }

    @Nested
    @DisplayName("updateContent()")
    class UpdateContentTests {

        @Test
        @DisplayName("유효한 내용 + 작성자이면 게시글 내용을 수정한다")
        void update_success() {
            // given
            Long postId = 1L;
            Long memberId = 1L;
            String newContent = "updated content";

            Member member = mock(Member.class);
            when(member.getNickname()).thenReturn("tester");

            Post post = mock(Post.class);
            when(post.checkOwnership(memberId)).thenReturn(true);
            when(post.getMember()).thenReturn(member);
            when(post.getContent()).thenReturn(newContent);
            // from() 에서 사용되는 나머지는 null/기본값으로 두거나 필요시 스텁

            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            GenericDataDto<String> dto = new GenericDataDto<>(newContent);

            // when
            PostResponseDto response = postService.updateContent(postId, dto, memberId);

            // then
            assertEquals(newContent, response.content());
            assertEquals("tester", response.userName());
            verify(post, times(1)).setContent(newContent);
            verify(postRepository, atLeastOnce()).findById(postId);
        }

        @Test
        @DisplayName("내용이 null 또는 빈 문자열이면 InvalidRequestException 발생")
        void update_invalid_content() {
            Long postId = 1L;
            Long memberId = 1L;

            GenericDataDto<String> emptyDto = new GenericDataDto<>("");

            assertThrows(InvalidRequestException.class,
                    () -> postService.updateContent(postId, emptyDto, memberId));
        }

        @Test
        @DisplayName("작성자가 아니면 ForbiddenException 발생")
        void update_forbidden() {
            // given
            Long postId = 1L;
            Long memberId = 1L;
            GenericDataDto<String> dto = new GenericDataDto<>("new");

            Post post = mock(Post.class);
            when(post.checkOwnership(memberId)).thenReturn(false);
            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            // when & then
            assertThrows(ForbiddenException.class,
                    () -> postService.updateContent(postId, dto, memberId));
            verify(post, never()).setContent(anyString());
        }

        @Test
        @DisplayName("게시글이 없으면 NotFoundException 발생")
        void update_not_found() {
            // given
            Long postId = 1L;
            Long memberId = 1L;
            GenericDataDto<String> dto = new GenericDataDto<>("new");

            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class,
                    () -> postService.updateContent(postId, dto, memberId));
        }
    }

    @Nested
    @DisplayName("findAsSlice(), findByMemberAsSlice()")
    class SliceTests {

        @Test
        @DisplayName("전체 게시글 슬라이스 조회는 Repository 에 위임한다")
        void findAsSlice() {
            // given
            Pageable pageable = PageRequest.of(0, 15);
            PostResponseDto dto = new PostResponseDto(
                    1L, "content", 1L, "tester", 0L, null, null, 0L
            );
            Slice<PostResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(postRepository.findPostSliceWithLikeCountAndCommentCount(pageable))
                    .thenReturn(slice);

            // when
            Slice<PostResponseDto> result = postService.findAsSlice(pageable);

            // then
            assertSame(slice, result);
            verify(postRepository, times(1))
                    .findPostSliceWithLikeCountAndCommentCount(pageable);
        }

        @Test
        @DisplayName("사용자별 게시글 슬라이스 조회는 Repository 에 위임한다")
        void findByMemberAsSlice() {
            // given
            Pageable pageable = PageRequest.of(0, 15);
            Long memberId = 1L;

            PostResponseDto dto = new PostResponseDto(
                    1L, "content", 1L, "tester", 0L, null, null, 0L
            );
            Slice<PostResponseDto> slice =
                    new SliceImpl<>(List.of(dto), pageable, false);

            when(postRepository.findPostSliceByMemberIdWithLikeCountAndCommentCount(pageable, memberId))
                    .thenReturn(slice);

            // when
            Slice<PostResponseDto> result = postService.findByMemberAsSlice(pageable, memberId);

            // then
            assertSame(slice, result);
            verify(postRepository, times(1))
                    .findPostSliceByMemberIdWithLikeCountAndCommentCount(pageable, memberId);
        }
    }
}
