package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import com.idukbaduk.itseats.member.dto.response.CurrentMemberResponse;
import com.idukbaduk.itseats.member.dto.response.MemberCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.FavoriteRepository;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public MemberCreateResponse createCustomer(CustomerDto customerDto) {

        if (memberRepository.existsByUsername(customerDto.getUsername())) {
            throw new MemberException(MemberErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberRepository.existsByNickname(customerDto.getNickname())) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (memberRepository.existsByEmail(customerDto.getEmail())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        String encryptedPassword = passwordEncoder.encode(customerDto.getPassword());
        Member newCustomer = memberRepository.save(customerDto.toEntity(encryptedPassword));
        return MemberCreateResponse.of(newCustomer.getMemberId());
    }

    public CurrentMemberResponse getCurrentMember(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        int reviewCount = reviewRepository.countByMember(member);
        int favoriteCount = favoriteRepository.countByMember(member);

        return CurrentMemberResponse.of(member, reviewCount, favoriteCount);
    }
}
