package com.idukbaduk.itseats.member.dto.request;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public record CustomerCreateRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String nickname,
        @Email String email,
        @NotBlank String phone
)
{
    public CustomerDto toDto() {
        return CustomerDto.builder()
                .username(this.username)
                .password(this.password)
                .nickname(this.nickname)
                .email(this.email)
                .phone(this.phone)
                .build();
    }
}
