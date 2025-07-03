package com.idukbaduk.itseats.store.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePauseRequest {
    @Min(value = 1, message = "일시정지 시간은 1분 이상이어야 합니다.")
    private int pauseTime;
}
