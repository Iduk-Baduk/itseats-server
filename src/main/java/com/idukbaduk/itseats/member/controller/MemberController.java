package com.idukbaduk.itseats.member.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.dto.request.CustomerCreateRequest;
import com.idukbaduk.itseats.member.dto.enums.MemberResponse;
import com.idukbaduk.itseats.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> register(@RequestBody @Valid CustomerCreateRequest customerCreateRequest) {
        return BaseResponse.toResponseEntity(
                MemberResponse.CREATE_MEMBER_SUCCESS,
                memberService.createCustomer(customerCreateRequest.toDto())
        );
    }

}
