package com.idukbaduk.itseats.member.dto;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.entity.enums.MemberType;
import com.idukbaduk.itseats.member.util.PasswordUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCustomerDto {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    @Builder
    public CreateCustomerDto(String username, String password, String nickname, String email, String phone) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
    }

    public Member toEntity() {
        return Member.builder()
                .username(this.username)
                .password(PasswordUtil.encrypt(this.password))
                .nickname(this.nickname)
                .email(this.email)
                .phone(this.phone)
                .memberType(MemberType.CUSTOMER)
                .build();
    }

}
