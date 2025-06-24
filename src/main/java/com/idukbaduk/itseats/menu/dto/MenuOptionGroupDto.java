package com.idukbaduk.itseats.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptionGroupDto {
    private String optionGroupName;
    private boolean isRequired;
    private int minSelect;
    private int maxSelect;
    private int priority;
    List<MenuOptionDto> options;
}
