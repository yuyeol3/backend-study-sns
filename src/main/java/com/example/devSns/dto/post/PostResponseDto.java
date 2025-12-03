package com.example.devSns.dto.post;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.comment.CommentResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
        Long id,
        String content,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("like_count") Long likeCount,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        Long comments
) {



    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getContent(),
                post.getMember().getId(),
                post.getMember().getNickname(),
                post.getPostLikes() == null ? 0L : post.getPostLikes().stream().count(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getComments() == null ? 0L : post.getComments().stream().count()
        );
    }
}
