package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.RiderDto;
import com.idukbaduk.itseats.member.dto.response.CurrentRiderResponse;
import com.idukbaduk.itseats.member.dto.response.MemberCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderMemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RiderRepository riderRepository;

    @Transactional
    public MemberCreateResponse createRider(RiderDto riderDto) {

        if (memberRepository.existsByUsername(riderDto.getUsername())) {
            throw new MemberException(MemberErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberRepository.existsByNickname(riderDto.getNickname())) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (memberRepository.existsByEmail(riderDto.getEmail())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        String encryptedPassword = passwordEncoder.encode(riderDto.getPassword());
        Member newRider = memberRepository.save(riderDto.toEntity(encryptedPassword));
        return MemberCreateResponse.of(newRider.getMemberId());
    }

    public CurrentRiderResponse getCurrentRider(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        Rider rider = riderRepository.findByMember(member).orElseThrow(
                () -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND)
        );

        return CurrentRiderResponse.of(member, rider);
    }
}
