package com.idukbaduk.itseats.menu.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "menu")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_group_id", nullable = false)
    private MenuGroup menuGroup;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private Long menuPrice;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "menu_status", nullable = false)
    private MenuStatus menuStatus;

    @Column(name = "menu_rating")
    private Float menuRating;

    @Column(name = "menu_description")
    private String menuDescription;

    @Column(name = "menu_priority", nullable = false)
    private int menuPriority;

    @Builder.Default
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuOptionGroup> menuOptionGroups = new ArrayList<>();

    public void setMenuOptionGroups(List<MenuOptionGroup> menuOptionGroups) {
        // 옵션 그룹 변경을 JPA가 감지하고 기존 옵션을 삭제하도록 clear() 호출
        this.menuOptionGroups.clear();
        menuOptionGroups.forEach(menuOptionGroup -> {
            if (menuOptionGroup.getMenu() != this)
                menuOptionGroup.setMenu(this);
        });
        this.menuOptionGroups.addAll(menuOptionGroups);
    }

    public void updateMenu(MenuGroup menuGroup, String menuName, Long menuPrice,
                           MenuStatus menuStatus, String menuDescription, int menuPriority) {
        this.menuGroup = menuGroup;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuStatus = menuStatus;
        this.menuDescription = menuDescription;
        this.menuPriority = menuPriority;
    }
}
