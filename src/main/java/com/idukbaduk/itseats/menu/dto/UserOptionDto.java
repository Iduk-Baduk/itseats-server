package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserOptionDto {
    private Long optionId;
    private String optionName;
    private Long optionPrice;
    private String optionStatus;
    private int optionPriority;
    private boolean isSelected;
}
