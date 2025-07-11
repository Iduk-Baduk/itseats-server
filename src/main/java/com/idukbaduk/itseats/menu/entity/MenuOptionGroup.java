package com.idukbaduk.itseats.menu.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "menu_option_group")
public class MenuOptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opt_group_id")
    private Long optGroupId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "opt_group_name", nullable = false)
    private String optGroupName;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "min_select", nullable = false)
    private int minSelect;

    @Column(name = "max_select", nullable = false)
    private int maxSelect;

    @Column(name = "opt_group_priority", nullable = false)
    private int optGroupPriority;

    @Builder.Default
    @OneToMany(mappedBy = "menuOptionGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuOption> options = new ArrayList<>();

    public void addOption(MenuOption menuOption) {
        options.add(menuOption);
        if (menuOption.getMenuOptionGroup() != this) {
            menuOption.setMenuOptionGroup(this);
        }
    }
}
