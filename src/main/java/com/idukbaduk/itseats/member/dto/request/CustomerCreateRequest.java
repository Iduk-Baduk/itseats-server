package com.idukbaduk.itseats.member.dto.request;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerCreateRequest(
        @NotBlank String username,

        @NotBlank @Pattern(
                regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "비밀번호는 최소 8자리, 영문, 숫자, 특수문자를 포함해야 합니다"
        ) String password,

        @NotBlank String name,

        @NotBlank String nickname,

        @Email String email,

        @NotBlank @Pattern(
                regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다"
        ) String phone
)
{
    public CustomerDto toDto() {
        return CustomerDto.builder()
                .username(this.username)
                .password(this.password)
                .name(this.name)
                .nickname(this.nickname)
                .email(this.email)
                .phone(this.phone)
                .build();
    }

}
