package com.example.devSns.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "comments_likes",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "u_member_comment",
                columnNames = {"comment_id", "member_id"}
        )
    }
)
public class CommentLikes extends BaseLikeEntity{

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    public CommentLikes() {}
    public CommentLikes(Member member, Comment comment) {
        super(member);
        this.comment = comment;
    }
}
