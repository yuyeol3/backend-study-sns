package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.dto.GenericDataDto;
import com.example.devSns.dto.post.PostCreateDto;
import com.example.devSns.dto.post.PostResponseDto;
import com.example.devSns.exception.ForbiddenException;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostService(PostRepository postRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long create(PostCreateDto postCreateDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new NotFoundException("member not found"));
        Post post = Post.create(postCreateDto.content(), member);
        return postRepository.save(post).getId();
    }

    public PostResponseDto findPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundException("post not found"));
        return PostResponseDto.from(post);
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundException("post not found"));
        if (!post.checkOwnership(memberId)) throw new ForbiddenException("Forbidden");
        postRepository.delete(post);
    }

    @Transactional
    public PostResponseDto updateContent(Long id, GenericDataDto<String> contentsDto, Long memberId) {
        if (contentsDto.data() == null || contentsDto.data().isEmpty())
            throw new InvalidRequestException("Invalid request.");

        Post post = postRepository.findById(id).orElseThrow(()->new NotFoundException("post not found"));
        if (!post.checkOwnership(memberId)) throw new ForbiddenException("Forbidden");
        post.setContent(contentsDto.data());

        return findPostById(id);
    }

    public Slice<PostResponseDto> findAsSlice(Pageable pageable) {
        return postRepository.findPostSliceWithLikeCountAndCommentCount(pageable);
    }

    public Slice<PostResponseDto> findByMemberAsSlice(Pageable pageable, Long memberId) {
        return postRepository.findPostSliceByMemberIdWithLikeCountAndCommentCount(pageable, memberId);
    }

}
