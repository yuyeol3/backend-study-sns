package com.example.devSns.repository;

import com.example.devSns.domain.Member;
import com.example.devSns.dto.member.MemberResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Slice<Member> findMembersByNickname(String nickname, Pageable pageable);
    List<Member> findMembersByIdIn(List<Long> id);

    @Query("""
        SELECT new com.example.devSns.dto.member.MemberResponseDto(
            f.follower.id, f.follower.nickname
        )
        FROM Follows f
        WHERE f.following.id = :id
    """)
    Slice<MemberResponseDto> findFollowings(Long id, Pageable pageable);

    @Query("""
        SELECT new com.example.devSns.dto.member.MemberResponseDto(
            f.following.id, f.following.nickname
        )
        FROM Follows f
        WHERE f.follower.id = :id
    """)
    Slice<MemberResponseDto> findFollowers(Long id, Pageable pageable);
}
