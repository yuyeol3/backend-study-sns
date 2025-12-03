package com.example.devSns.controller;

import com.example.devSns.annotation.LoginUser;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.post.PostCreateDto;
import com.example.devSns.dto.post.PostResponseDto;
import com.example.devSns.service.PostService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<GenericDataDto<Long>> create(
            @RequestBody @Valid PostCreateDto postCreateDto, @LoginUser Long memberId
    ) {
        Long id = postService.create(postCreateDto, memberId);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).body(new GenericDataDto<>(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getOne(@PathVariable @Positive Long id) {
        PostResponseDto post = postService.findPostById(id);
        return ResponseEntity.ok().body(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id, @LoginUser Long memberId) {
        postService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<Slice<PostResponseDto>> getAsPaginated(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @RequestParam @Nullable Long memberId
    ) {
        Slice<PostResponseDto> posts = memberId == null ?
                postService.findAsSlice(pageable) : postService.findByMemberAsSlice(pageable, memberId);
        return ResponseEntity.ok().body(posts);
    }



    @PatchMapping("/{id}/contents")
    public ResponseEntity<PostResponseDto> contents(
            @PathVariable @Positive Long id, @RequestBody @Valid GenericDataDto<String> contentsDto, @LoginUser Long memberId) {

        PostResponseDto post = postService.updateContent(id, contentsDto, memberId);
        return ResponseEntity.ok().body(post);
    }

}
