package com.idukbaduk.itseats.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuListRequest {
    private String menuGroup;
    private String keyword;
}
