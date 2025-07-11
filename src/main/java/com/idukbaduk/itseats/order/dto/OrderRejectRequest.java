package com.idukbaduk.itseats.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRejectRequest {
    @NotBlank(message = "거절 사유는 필수입니다")
    private String reason;
}
