package com.example.devSns.service;

import com.example.devSns.domain.Post;
import com.example.devSns.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikesServiceTests {

//    @Test
//    @DisplayName("게시글 좋아요 성공")
//    void like_success() {
//        // given
//        Long postId = 1L;
//        Post post = createDummyPost(postId, "Content", "User");
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//
//        // when
//        postService.like(postId);
//
//        // then
//        verify(postRepository, times(1)).findById(postId);
//        verify(postRepository, times(1)).incrementLikeById(postId);
//    }

//    @Test
//    @DisplayName("존재하지 않는 게시글에 좋아요 시 NotFoundException 발생")
//    void like_throwsNotFoundException() {
//        // given
//        Long postId = 99L;
//        when(postRepository.findById(postId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(NotFoundException.class, () -> postService.like(postId));
//        verify(postRepository, never()).incrementLikeById(anyLong());
//    }
}
