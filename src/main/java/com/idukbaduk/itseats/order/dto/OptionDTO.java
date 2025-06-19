package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionDTO {

    private String optionName;
    private int optionPrice;
}
