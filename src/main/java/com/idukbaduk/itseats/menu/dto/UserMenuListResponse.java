package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserMenuListResponse {
    private List<UserMenuGroupDto> menuGroups;
}
