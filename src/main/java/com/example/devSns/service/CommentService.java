package com.example.devSns.service;

import com.example.devSns.domain.Comment;
import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.PaginatedDto;
import com.example.devSns.dto.comment.CommentCreateDto;
import com.example.devSns.dto.comment.CommentResponseDto;
import com.example.devSns.exception.ForbiddenException;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.CommentRepository;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long create(CommentCreateDto commentCreateDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        Post post = postRepository.findById(commentCreateDto.postId())
                .orElseThrow(()->new InvalidRequestException("Invalid Request."));

        Comment comment = new Comment(commentCreateDto.content(), post, member);
        return commentRepository.save(comment).getId();
    }

    public CommentResponseDto findCommentById(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(()->new NotFoundException("comment not found"));

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        Comment comment = commentRepository.findById(id).orElseThrow(()->new NotFoundException("comment not found"));
        if (!comment.checkOwnership(memberId)) throw new ForbiddenException("Forbidden");
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDto updateContent(Long id, GenericDataDto<String> contentsDto, Long memberId) {
        if (contentsDto.data() == null || contentsDto.data().isEmpty())
            throw new InvalidRequestException("Invalid Request.");

        Comment comment = commentRepository.findById(id).orElseThrow(()->new NotFoundException("comment not found"));
        if (!comment.checkOwnership(memberId)) throw new ForbiddenException("Forbidden");
        comment.setContent(contentsDto.data());

        return findCommentById(id);
    }

    public PaginatedDto<List<CommentResponseDto>> findAsPaginated(GenericDataDto<Long> idDto, Long postId) {
        Long criteria = idDto.data();
        List<Comment> comments = criteria == null ?
                commentRepository.findTop15ByCreatedAtBeforeAndPostIdOrderByCreatedAtDescPostIdDesc(LocalDateTime.now().plusSeconds(1), postId) :
                commentRepository.findTop15ByIdBeforeAndPostIdOrderByIdDesc(criteria, postId);

        if (comments.isEmpty()) {
            return new PaginatedDto<>(List.of(), null);
        }

        List<CommentResponseDto> commentResponseDtoList = comments
                .stream().map(CommentResponseDto::from).toList();
        Long nextQueryCriteria = comments.getLast().getId();

        return new PaginatedDto<>(commentResponseDtoList, nextQueryCriteria);
    }

    public Slice<CommentResponseDto> findAsSlice(Pageable pageable, Long postId) {
        return commentRepository.findCommentSliceByPostIdWithLikeCount(pageable, postId);
    }

    public Slice<CommentResponseDto> findByMemberAsSlice(Pageable pageable, Long memberId) {
        return commentRepository.findCommentSliceByMemberIdWithLikeCount(pageable, memberId);
    }
}
