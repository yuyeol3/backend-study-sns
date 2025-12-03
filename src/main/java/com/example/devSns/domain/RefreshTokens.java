package com.example.devSns.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokens {

    @Id
    @Column(columnDefinition = "BINARY(32)")
    private byte []id;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "valid", nullable = false)
    private Boolean valid = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public RefreshTokens() {}
    public RefreshTokens(byte []id, LocalDateTime validUntil, Member member) {
        this.id = id;
        this.validUntil = validUntil;
        this.member = member;
    }

    public void disable() {
        this.valid = false;
    }

    public boolean isValidToken() {
        return (this.valid) && (this.validUntil.isAfter(LocalDateTime.now()));
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return this.member;
    }

    public byte[] getTokenHash() {
        return id;
    }

}
