package com.idukbaduk.itseats.rider.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NearByOrderRequest {

    @NotNull(message = "위도는 필수값입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수값입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    private Double longitude;
}
