package com.example.devSns.dto.follow;

import com.example.devSns.domain.Member;

public record FollowsDto(Member follower, Member following) {}
