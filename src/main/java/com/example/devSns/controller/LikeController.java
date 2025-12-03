package com.example.devSns.controller;


import com.example.devSns.annotation.LoginUser;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.likes.LikesRequestDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.dto.post.PostResponseDto;
import com.example.devSns.service.LikesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.java.Log;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class LikeController<T> {
    private final LikesService<T> likeService;

    public LikeController(LikesService<T> likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Void> like(@PathVariable @Positive Long id, @LoginUser Long memberId) {
        likeService.like(new LikesRequestDto(id, memberId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(@PathVariable @Positive Long id, @LoginUser Long memberId) {
        likeService.unlike(new LikesRequestDto(id, memberId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Slice<MemberResponseDto>> getLikes(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @PathVariable @Positive Long id
    ) {
        Slice<MemberResponseDto> likedMembers = likeService.findWhoLiked(pageable, id);
        return ResponseEntity.ok(likedMembers);
    }

}
