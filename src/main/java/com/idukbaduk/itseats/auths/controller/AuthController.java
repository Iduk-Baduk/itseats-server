package com.idukbaduk.itseats.auths.controller;

import com.idukbaduk.itseats.auths.service.AuthService;
import com.idukbaduk.itseats.global.response.BaseResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<BaseResponse> login(
            @RequestParam("memberId") @NotBlank String memberId
    ) {
        // todo: 추후 구현
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse> logout(
            @RequestParam("memberId") String memberId
    ) {
        // todo: 추후 구현
        return null;
    }

    @GetMapping("/reissue")
    public ResponseEntity<BaseResponse> reissueAccessToken(
            @RequestHeader("refresh-token") String refreshToken,
            @RequestParam("memberId") String memberId
    ) {
        // todo: 추후 구현
        return null;
    }

}