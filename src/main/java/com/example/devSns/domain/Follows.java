package com.example.devSns.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "member_follows")
public class Follows extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;

    public Follows() {}
    public Follows(Member follower, Member following) {
        this.follower = follower;
        this.following = following;
    }

    public Long getId() {
        return id;
    }

    public Member getFollower() {
        return follower;
    }

    public Member getFollowing() {
        return following;
    }
}
