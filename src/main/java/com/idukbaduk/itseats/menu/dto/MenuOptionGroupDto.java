package com.idukbaduk.itseats.menu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptionGroupDto {
    @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
    private String optionGroupName;

    private boolean isRequired;

    @Min(value = 0, message = "최소 선택 수는 0 이상이어야 합니다.")
    private int minSelect;

    @Min(value = 1, message = "최대 선택 수는 1 이상이어야 합니다.")
    private int maxSelect;

    @Min(value = 0, message = "옵션 그룹 우선순위는 0 이상이어야 합니다.")
    private int priority;

    @NotEmpty(message = "옵션 목록은 하나 이상 포함되어야 합니다.")
    @Valid
    List<MenuOptionDto> options;
}
