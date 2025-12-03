package com.example.devSns.repository;

import com.example.devSns.domain.Member;
import com.example.devSns.dto.member.MemberResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Slice<Member> findMembersByNickname(String nickname, Pageable pageable);
    List<Member> findMembersByIdIn(List<Long> id);


}
