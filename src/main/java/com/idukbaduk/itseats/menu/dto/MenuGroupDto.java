package com.idukbaduk.itseats.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuGroupDto {
    private String menuGroupName;
    private int menuGroupPriority;
    private boolean menuGroupIsActive;
}
