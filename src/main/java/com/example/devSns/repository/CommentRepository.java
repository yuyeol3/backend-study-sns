package com.example.devSns.repository;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.comment.CommentResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {



    List<Comment> findTop15ByIdBeforeAndPostIdOrderByIdDesc(Long id, Long postId);
    List<Comment> findTop15ByCreatedAtBeforeAndPostIdOrderByCreatedAtDescPostIdDesc(LocalDateTime createdAt, Long postId);

    @Query("""
        SELECT new com.example.devSns.dto.comment.CommentResponseDto(
            c.id,
            c.post.id,
            c.content,
            m.id,
            m.nickname,
            (select count(*) as likes from CommentLikes cl where c = cl.comment),
            c.createdAt,
            c.updatedAt
        )
        FROM Comment c
        JOIN c.member m
        Where c.post.id = :postId
    """)
    Slice<CommentResponseDto> findCommentSliceByPostIdWithLikeCount(Pageable pageable, Long postId);


    @Query("""
     SELECT new com.example.devSns.dto.comment.CommentResponseDto(
            c.id,
            c.post.id,
            c.content,
            m.id,
            m.nickname,
            (select count(*) as likes from CommentLikes cl where c = cl.comment),
            c.createdAt,
            c.updatedAt
        )
        FROM Comment c
        JOIN c.member m
        Where m.id = :memberId
    """)
    Slice<CommentResponseDto> findCommentSliceByMemberIdWithLikeCount(Pageable pageable, Long memberId);

    Long countCommentsByPostId(Long postId);

    @Query("select p.id, count(*) from Post p join Comment c on p = c.post where p in :posts group by p.id ")
    public List<Long[]> countCommentsAndGroupByPostIdIn(List<Post> posts);
}
