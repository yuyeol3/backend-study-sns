package com.example.devSns.domain;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseLikeEntity extends BaseTimeEntity implements OwnableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public BaseLikeEntity() {}
    public BaseLikeEntity(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public Long getId() {
        return id;
    }
}
