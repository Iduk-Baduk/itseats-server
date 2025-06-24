package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptionDto {
    private String optionName;
    private long optionPrice;
    private MenuStatus optionStatus;
    private int optionPriority;
}
