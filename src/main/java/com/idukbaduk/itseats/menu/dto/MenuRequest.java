package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
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
    List<MenuOptionGroupDto> optionGroups;
    List<MultipartFile> images;
}
