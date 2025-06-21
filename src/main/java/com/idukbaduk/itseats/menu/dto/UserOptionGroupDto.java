package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserOptionGroupDto {
    private String optionGroupName;
    private boolean isRequired;
    private int minSelect;
    private int maxSelect;
    private int priority;
    private List<UserOptionDto> options;
}
