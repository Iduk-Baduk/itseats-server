package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.request.CustomerCreateRequest;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public ResponseEntity<BaseResponse> signUp(@RequestBody @Valid CustomerCreateRequest customerCreateRequest) {
        return BaseResponse.toResponseEntity(
                MemberResponse.CREATE_MEMBER_SUCCESS,
                memberService.createCustomer(customerCreateRequest.toDto())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return BaseResponse.toResponseEntity(
                MemberResponse.GET_CURRENT_MEMBER_SUCCESS,
                memberService.getCurrentMember(userDetails.getUsername())
        );
    }
}
