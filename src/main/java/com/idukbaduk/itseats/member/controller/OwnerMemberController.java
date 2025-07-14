package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.dto.request.OwnerCreateRequest;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.service.OwnerMemberService;
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
@RequestMapping("/api/owner/members")
public class OwnerMemberController {

    private final OwnerMemberService ownerMemberService;

    @PostMapping("/sign-up")
    public ResponseEntity<BaseResponse> signUp(@RequestBody @Valid OwnerCreateRequest ownerCreateRequest) {
        return BaseResponse.toResponseEntity(
                MemberResponse.CREATE_MEMBER_SUCCESS,
                ownerMemberService.createOwner(ownerCreateRequest.toDto())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_USER);
        }
        return BaseResponse.toResponseEntity(
                MemberResponse.GET_CURRENT_MEMBER_SUCCESS,
                ownerMemberService.getCurrentOwner(userDetails.getUsername())
        );
    }
}
