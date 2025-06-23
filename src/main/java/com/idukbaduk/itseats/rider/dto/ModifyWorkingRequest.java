package com.idukbaduk.itseats.rider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModifyWorkingRequest {

    @NotNull(message = "출/퇴근 여부는 필수값입니다.")
    private Boolean isWorking;
}
