package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.dto.request.OwnerCreateRequest;
import com.idukbaduk.itseats.member.service.OwnerMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
