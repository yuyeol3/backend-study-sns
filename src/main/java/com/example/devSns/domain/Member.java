package com.example.devSns.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="email", nullable = false, unique = true)
    private String email;
    
    @Column(name="password", nullable = false)
    private String password;
    
    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    private List<Post> posts;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "follower", cascade = CascadeType.ALL)
    private List<Follows> followList; // 내가 팔로우하는 사람 목록

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "following", cascade = CascadeType.ALL)
    private List<Follows> followingList; // 나를 팔로우한 사람 목록

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    private List<PostLikes> postLikes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    private List<CommentLikes> commentLikes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshTokens> refreshTokens = new ArrayList<>();

    @Version
    private Long version;

    public Member() {}
    public Member(String nickname, String email, String passwordHash) {
        this.nickname = nickname;
        this.email = email;
        this.password = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }

    public void addRefreshToken(RefreshTokens refreshToken) {
        refreshTokens.add(refreshToken);
    }

}
