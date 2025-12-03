package com.example.devSns.dto.comment;

import com.example.devSns.domain.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CommentResponseDto(
    Long id,
    @JsonProperty("post_id") Long postId,
    String content,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("user_name") String userName,
    @JsonProperty("like_count") Long likeCount,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {

    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getCommentLikes().stream().count(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
