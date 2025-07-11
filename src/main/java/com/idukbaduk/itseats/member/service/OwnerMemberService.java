package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.OwnerDto;
import com.idukbaduk.itseats.member.dto.StoreIdAndStoreNameDto;
import com.idukbaduk.itseats.member.dto.response.CurrentOwnerResponse;
import com.idukbaduk.itseats.member.dto.response.MemberCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerMemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MemberCreateResponse createOwner(OwnerDto ownerDto) {

        if (memberRepository.existsByUsername(ownerDto.getUsername())) {
            throw new MemberException(MemberErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberRepository.existsByNickname(ownerDto.getNickname())) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (memberRepository.existsByEmail(ownerDto.getEmail())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        String encryptedPassword = passwordEncoder.encode(ownerDto.getPassword());
        Member newCustomer = memberRepository.save(ownerDto.toEntity(encryptedPassword));
        return MemberCreateResponse.of(newCustomer.getMemberId());
    }

    public CurrentOwnerResponse getCurrentOwner(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        List<Store> ownStores = storeRepository.findByMember(member);

        return CurrentOwnerResponse.of(member, ownStores.stream().map(StoreIdAndStoreNameDto::of).toList());
    }
}
