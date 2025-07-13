package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.dto.request.RiderCreateRequest;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.service.RiderMemberService;
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
@RequestMapping("/api/rider/members")
public class RiderMemberController {

    private final RiderMemberService riderMemberService;

    @PostMapping("/sign-up")
    public ResponseEntity<BaseResponse> signUp(@RequestBody @Valid RiderCreateRequest riderCreateRequest) {
        return BaseResponse.toResponseEntity(
                MemberResponse.CREATE_MEMBER_SUCCESS,
                riderMemberService.createRider(riderCreateRequest.toDto())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_USER);
        }
        return BaseResponse.toResponseEntity(
                MemberResponse.GET_CURRENT_MEMBER_SUCCESS,
                riderMemberService.getCurrentRider(userDetails.getUsername())
        );
    }
}
