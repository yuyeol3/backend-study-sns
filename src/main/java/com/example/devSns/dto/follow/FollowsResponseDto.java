package com.example.devSns.dto.follow;

import com.example.devSns.dto.member.MemberResponseDto;

public record FollowsResponseDto(
        MemberResponseDto follower,
        MemberResponseDto following
) {
}
