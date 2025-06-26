package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String menuName;

    @NotBlank(message = "메뉴 설명은 필수입니다.")
    private String menuDescription;

    @Min(value = 0,  message = "메뉴 가격은 0원 이상이어야 합니다.")
    private long menuPrice;

    @NotNull(message = "메뉴 상태는 필수입니다.")
    private MenuStatus menuStatus;

    @NotBlank(message = "메뉴 그룹 이름은 필수입니다.")
    private String menuGroupName;

    @Min(value = 0, message = "메뉴 우선순위는 0 이상이어야 합니다.")
    private int menuPriority;

    @Valid
    List<MenuOptionGroupDto> optionGroups;
}
