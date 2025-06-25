package com.idukbaduk.itseats.menu.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
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
