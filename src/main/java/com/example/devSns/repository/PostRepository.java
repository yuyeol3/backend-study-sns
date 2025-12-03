package com.example.devSns.repository;

import com.example.devSns.domain.Post;
import com.example.devSns.dto.post.PostResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

//    @Modifying
//    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :id ")
//    void incrementLikeById(Long id);

    List<Post> findTop15ByIdBeforeOrderByIdDesc(Long id);
    List<Post> findTop15ByCreatedAtBeforeOrderByCreatedAtDesc(LocalDateTime createdAt);

    @Query("""
        SELECT NEW com.example.devSns.dto.post.PostResponseDto(
            p.id,
            p.content,
            m.id,
            m.nickname,
            (select count(*) as likes from PostLikes pl where p = pl.post),
            p.createdAt,
            p.updatedAt,
            (select count(*) as comments from Comment c where p = c.post)
        )
        FROM Post p
        JOIN p.member m
    """)
    Slice<PostResponseDto> findPostSliceWithLikeCountAndCommentCount(Pageable pageable);

    @Query("""
    SELECT NEW com.example.devSns.dto.post.PostResponseDto(
            p.id,
            p.content,
            m.id,
            m.nickname,
            (select count(*) as likes from PostLikes pl where p = pl.post),
            p.createdAt,
            p.updatedAt,
            (select count(*) as comments from Comment c where p = c.post)
        )
        FROM Post p
        JOIN p.member m
        WHERE m.id = :memberId
    """)
    Slice<PostResponseDto> findPostSliceByMemberIdWithLikeCountAndCommentCount(Pageable pageable, Long memberId);
//    Slice<Post> findByOrderByIdDesc(Pageable pageable);
}
