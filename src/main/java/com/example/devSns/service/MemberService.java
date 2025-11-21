package com.example.devSns.service;

import com.example.devSns.domain.Member;
import com.example.devSns.dto.member.MemberCreateDto;
import com.example.devSns.dto.member.MemberResponseDto;
import com.example.devSns.exception.InvalidRequestException;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long create(MemberCreateDto memberCreateDto) {
        if (memberRepository.findByEmail(memberCreateDto.email()).isPresent()) {
            throw new InvalidRequestException("Already signed up");
        }

        String passwordHash = BCrypt.hashpw(memberCreateDto.password(), BCrypt.gensalt());
        Member member = new Member(memberCreateDto.nickname(), memberCreateDto.email(), passwordHash);
        return memberRepository.save(member).getId();
    }

    public MemberResponseDto getOne(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new NotFoundException("Member not found"));

        return new MemberResponseDto(member.getId(), member.getNickname());
    }

    public Slice<MemberResponseDto> findByNickname(Pageable pageable,String nickname) {
        Slice<Member> members = memberRepository.findMembersByNickname(nickname, pageable);
        return members.map(MemberResponseDto::from);
    }

    public Slice<MemberResponseDto> findFollowers(Pageable pageable, Long memberId) {
        return memberRepository.findFollowers(memberId, pageable);
    }

    public Slice<MemberResponseDto> findFollowing(Pageable pageable, Long memberId) {
        return memberRepository.findFollowings(memberId, pageable);
    }


}
