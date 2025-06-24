package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private String menuName;
    private String menuDescription;
    private long menuPrice;
    private MenuStatus menuStatus;
    private String menuGroupName;
    private int menuPriority;
    private List<MultipartFile> images;
    List<MenuOptionGroupDto> optionGroups;
}
