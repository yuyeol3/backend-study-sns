package com.example.devSns.service;

import com.example.devSns.domain.Follows;
import com.example.devSns.domain.Member;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.follow.FollowRequestDto;
import com.example.devSns.dto.follow.FollowsDto;
import com.example.devSns.dto.follow.FollowsResponseDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.FollowsRepository;
import com.example.devSns.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class FollowsService {
    private final FollowsRepository followsRepository;
    private final MemberRepository memberRepository;

    public FollowsService(FollowsRepository followsRepository, MemberRepository memberRepository) {
        this.followsRepository = followsRepository;
        this.memberRepository = memberRepository;
    }

    public FollowsResponseDto findFollows(FollowRequestDto followRequestDto) {
        Follows follows = followsRepository.findByFollowerIdAndFollowingId(followRequestDto.followerId(), followRequestDto.followingId())
                .orElseThrow(()->new NotFoundException("Follows not found"));

        return new FollowsResponseDto(
                MemberResponseDto.from(follows.getFollower()),
                MemberResponseDto.from(follows.getFollowing())
        );
    }

    @Transactional
    public Long follow(FollowRequestDto followRequestDto) {
        if (followRequestDto.followerId().equals(followRequestDto.followingId()))
            throw new InvalidRequestException("You can't follow yourself");

        FollowsDto fd = findFollowerAndFollowing(followRequestDto);
        boolean followExists = followsRepository.findByFollowerAndFollowing(fd.follower(), fd.following()).isPresent();
        if (followExists) throw new InvalidRequestException("Follower already exists");

        Follows follows = new Follows(fd.follower(), fd.following());
        return followsRepository.save(follows).getId();
    }

    @Transactional
    public void unfollow(FollowRequestDto followRequestDto) {
        FollowsDto fd = findFollowerAndFollowing(followRequestDto);
        Follows follows = followsRepository.findByFollowerAndFollowing(fd.follower(), fd.following())
                .orElseThrow(() -> new NotFoundException("Follower not found"));
        followsRepository.delete(follows);
    }


    private FollowsDto findFollowerAndFollowing(FollowRequestDto followRequestDto) {
        Member follower = memberRepository.findById(followRequestDto.followerId())
                .orElseThrow(()->new NotFoundException("Follower not found"));
        Member following = memberRepository.findById(followRequestDto.followingId())
                .orElseThrow(()->new NotFoundException("Following not found"));

        return new FollowsDto(follower, following);
    }

    public Slice<MemberResponseDto> findFollowers(Pageable pageable, Long memberId) {
        return followsRepository.findFollowersOf(memberId, pageable);
    }

    public Slice<MemberResponseDto> findFollowing(Pageable pageable, Long memberId) {
        return followsRepository.findFollowingsOf(memberId, pageable);
    }



}
