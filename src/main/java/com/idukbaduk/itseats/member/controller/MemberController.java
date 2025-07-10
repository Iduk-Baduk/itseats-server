package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.request.CustomerCreateRequest;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
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

    /**
     * 현재 로그인한 사용자 정보 조회
     * 
     * JWT 토큰을 통해 인증된 사용자의 정보를 반환합니다.
     * 
     * @param userDetails Spring Security에서 주입하는 사용자 정보
     *                    JWT 인증 필터에서 설정된 인증 정보
     * @return 현재 사용자 정보
     * @throws RuntimeException 인증되지 않은 사용자인 경우
     */
    @GetMapping("/me")
    public ResponseEntity<BaseResponse> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        // JWT 인증 필터에서 설정된 인증 정보가 없는 경우 처리
        if (userDetails == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_USER);
        }
        return BaseResponse.toResponseEntity(
                MemberResponse.GET_CURRENT_MEMBER_SUCCESS,
                memberService.getCurrentMember(userDetails.getUsername())
        );
    }
}
