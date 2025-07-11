package com.idukbaduk.itseats.order.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookTimeRequest {
    @Min(value = 1, message = "예상 조리 시간은 1분 이상이어야 합니다.")
    private int cookTime;
}
