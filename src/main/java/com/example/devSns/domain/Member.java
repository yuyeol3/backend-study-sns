package com.example.devSns.domain;

import jakarta.persistence.*;

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

    @Version
    private Long version;

    public Member() {}
    // todo : email, password 포함하는 생성자로 바꾸기
    public Member(String nickname) {
        this.nickname = nickname;
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

}
