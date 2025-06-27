package com.idukbaduk.itseats.member.dto.request;

import com.idukbaduk.itseats.member.dto.CreateCustomerDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public record CustomerCreateRequest(
        @NotBlank String username,
        String password,
        @NotBlank String nickname,
        @Email String email,
        String phone
)
{
    public CreateCustomerDto toDto() {
        return CreateCustomerDto.builder()
                .username(this.username)
                .password(this.password)
                .nickname(this.nickname)
                .email(this.email)
                .phone(this.phone)
                .build();
    }
}
