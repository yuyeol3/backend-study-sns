package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.domain.Post;
import com.example.devSns.domain.PostLikes;
import com.example.devSns.dto.likes.LikesResponseDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import com.example.devSns.repository.PostLikesRepository;
import com.example.devSns.repository.PostRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostLikesService extends LikesService<PostLikes> {
    private final PostLikesRepository postLikesRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public PostLikesService(PostLikesRepository postLikesRepository, MemberRepository memberRepository, PostRepository postRepository) {
        this.postLikesRepository = postLikesRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
    }

    @Override
    protected PostLikes createLikes(Long targetId, Long memberId) {
        MemberAndPost mp = findMemberAndPost(targetId, memberId);

        return new PostLikes(mp.member(), mp.post());
    }

    @Override
    protected Optional<PostLikes> findLikes(Long targetId, Long memberId) {
        MemberAndPost mp = findMemberAndPost(targetId, memberId);

        return postLikesRepository.findByMemberAndPost(mp.member(), mp.post());
    }

    @Override
    protected void saveLikes(PostLikes postLikes) {
        postLikesRepository.save(postLikes);
    }

    @Override
    protected void deleteLikes(PostLikes postLikes) {
        postLikesRepository.delete(postLikes);
    }

    @Override
    public Slice<MemberResponseDto> findWhoLiked(Pageable pageable, Long targetId) {
        Post post = postRepository.findById(targetId).orElseThrow(()->new NotFoundException("Post not found"));
        return postLikesRepository.findLikedUsersByPost(post, pageable);
    }

    private record MemberAndPost(Member member, Post post) {}
    private MemberAndPost findMemberAndPost(Long targetId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(()->new NotFoundException("Member not found"));
        Post post = postRepository.findById(targetId).orElseThrow(()->new NotFoundException("Post not found"));

        return new MemberAndPost(member, post);
    }

}
