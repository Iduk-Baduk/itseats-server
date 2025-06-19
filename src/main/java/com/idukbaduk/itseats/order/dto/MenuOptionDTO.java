package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuOptionDTO {

    private String optionGroupName;
    private List<OptionDTO> options;
}
