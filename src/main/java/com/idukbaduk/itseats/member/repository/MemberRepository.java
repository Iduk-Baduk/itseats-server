package com.idukbaduk.itseats.member.repository;

import static com.idukbaduk.itseats.member.error.enums.MemberErrorCode.MEMBER_NOT_FOUND;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getOrThrow(Long memberId) {
        return findByMemberId(memberId).orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickName);

}
