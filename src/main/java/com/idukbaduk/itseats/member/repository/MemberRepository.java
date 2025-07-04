package com.idukbaduk.itseats.member.repository;

import static com.idukbaduk.itseats.member.error.enums.MemberErrorCode.MEMBER_NOT_FOUND;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getOrThrowById(Long memberId) {
        return findByMemberId(memberId).orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }

    default Member getOrThrowByUsername(String username) {
        return findByUsername(username).orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickName);

}
