package com.example.devSns.controller;

import com.example.devSns.annotation.LoginUser;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.follow.FollowRequestDto;
import com.example.devSns.dto.member.MemberCreateDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.service.FollowsService;
import com.example.devSns.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final FollowsService followsService;
    public MemberController(MemberService memberService, FollowsService followsService) {
        this.memberService = memberService;
        this.followsService = followsService;
    }

    @PostMapping
    public ResponseEntity<GenericDataDto<Long>> create(@RequestBody @Valid MemberCreateDto memberCreateDto) {
        Long id = memberService.create(memberCreateDto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();


        return ResponseEntity.created(uri).body(new GenericDataDto<>(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long id) {
        MemberResponseDto memberResponseDto = memberService.getOne(id);
        return ResponseEntity.ok(memberResponseDto);
    }

    @GetMapping
    public ResponseEntity<Slice<MemberResponseDto>> getMembersByNickname(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @RequestParam @NotEmpty String nickname
    ) {
        Slice<MemberResponseDto> memberResponseDtoSlice = memberService.findByNickname(pageable, nickname);
        return ResponseEntity.ok(memberResponseDtoSlice);
    }


    @GetMapping("/{id}/follower")
    public ResponseEntity<Slice<MemberResponseDto>> getFollowers(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @PathVariable Long id
    ) {
        Slice<MemberResponseDto> members = followsService.findFollowers(pageable, id);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Slice<MemberResponseDto>> getFollowing(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @PathVariable Long id
    ) {
        Slice<MemberResponseDto> members = followsService.findFollowing(pageable, id);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/follower")
    public ResponseEntity<Void> follow(@PathVariable Long id, @LoginUser Long memberId) {
        Long followId = followsService.follow(new FollowRequestDto(id, memberId));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(followId)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{id}/follower")
    public ResponseEntity<Void> unfollow(@PathVariable Long id, @LoginUser Long memberId) {
        followsService.unfollow(new FollowRequestDto(id, memberId));
        return ResponseEntity.noContent().build();
    }
}
