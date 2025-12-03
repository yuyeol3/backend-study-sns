package com.example.devSns.controller;

import com.example.devSns.annotation.LoginUser;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.comment.CommentCreateDto;
import com.example.devSns.dto.comment.CommentResponseDto;
import com.example.devSns.service.CommentService;
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
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    @PostMapping
    public ResponseEntity<GenericDataDto<Long>> create(@RequestBody @Valid CommentCreateDto commentCreateDto, @LoginUser Long memberId) {
        Long id = commentService.create(commentCreateDto, memberId);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).body(new GenericDataDto<>(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDto> getOne(@PathVariable @Positive Long id) {
        CommentResponseDto comment = commentService.findCommentById(id);
        return ResponseEntity.ok().body(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id, @LoginUser Long memberId) {
        commentService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contents")
    public ResponseEntity<CommentResponseDto> contents(@PathVariable @Positive Long id,
                                                       @RequestBody @Valid GenericDataDto<String> contentsDto,
                                                       @LoginUser Long memberId) {
        CommentResponseDto comment = commentService.updateContent(id, contentsDto, memberId);
        return ResponseEntity.ok().body(comment);
    }

    @GetMapping
    public ResponseEntity<Slice<CommentResponseDto>> findByMemberIdAsPaginated(
            @PageableDefault(
                    size = 15,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @RequestParam @Positive Long memberId
    ) {
        Slice<CommentResponseDto> comments = commentService.findByMemberAsSlice(pageable, memberId);
        return ResponseEntity.ok().body(comments);
    }
}
