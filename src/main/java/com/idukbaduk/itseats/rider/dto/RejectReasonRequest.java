package com.idukbaduk.itseats.rider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectReasonRequest {

    @NotNull(message = "거절 사유는 필수값입니다.")
    private String rejectReason;
}
