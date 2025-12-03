package com.example.devSns.repository;

import com.example.devSns.domain.Follows;
import com.example.devSns.domain.Member;
import com.example.devSns.dto.member.MemberResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FollowsRepository extends JpaRepository<Follows, Long> {
    Optional<Follows> findByFollowerAndFollowing(Member follower, Member following);
    Optional<Follows> findByFollowerIdAndFollowingId(Long followerId, Long followingId);


    @Query("""
        SELECT new com.example.devSns.dto.member.MemberResponseDto(
            f.follower.id, f.follower.nickname
        )
        FROM Follows f
        WHERE f.following.id = :id
    """)
    Slice<MemberResponseDto> findFollowersOf(Long id, Pageable pageable);

    @Query("""
        SELECT new com.example.devSns.dto.member.MemberResponseDto(
            f.following.id, f.following.nickname
        )
        FROM Follows f
        WHERE f.follower.id = :id
    """)
    Slice<MemberResponseDto> findFollowingsOf(Long id, Pageable pageable);
}
