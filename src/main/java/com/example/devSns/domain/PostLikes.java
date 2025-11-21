package com.example.devSns.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "posts_likes",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "u_member_post",
                columnNames = {"post_id", "member_id"}
        )
    }
)
public class PostLikes extends BaseLikeEntity{

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostLikes() {}
    public PostLikes(Member member, Post post) {
        super(member);
        this.post = post;
    }


}
