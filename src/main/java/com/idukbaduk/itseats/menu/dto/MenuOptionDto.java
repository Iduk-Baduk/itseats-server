package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptionDto {
    @NotBlank(message = "옵션 이름은 필수입니다.")
    private String optionName;

    @Min(value = 0, message = "옵션 가격은 0 이상이어야 합니다.")
    private long optionPrice;

    @NotNull(message = "옵션 상태는 필수입니다.")
    private MenuStatus optionStatus;

    @Min(value = 0, message = "옵션 우선순위는 0 이상이어야 합니다.")
    private int optionPriority;
}
