package com.example.devSns.domain;


import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "posts")
public class Post extends BaseTimeEntity implements OwnableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostLikes> postLikes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Version
    private Long version;


    public Post() {}
    public Post(String content, Member member) {
        this.content = content;
        this.member = member;
    }

    public static Post create(String content, Member member) {
        Post post = new Post();
        post.content = content;
        post.member = member;
        return post;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<PostLikes> getPostLikes() {
        return postLikes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Member getMember() {
        return member;
    }


}
