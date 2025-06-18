package com.idukbaduk.itseats.menu.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "menu_group")
public class MenuGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_group_id")
    private Long menuGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "menu_group_name", nullable = false)
    private String menuGroupName;

    @Column(name = "menu_group_priority", nullable = false)
    private int menuGroupPriority;

    @Column(name = "menu_group_is_active", nullable = false)
    private boolean menuGroupIsActive;

    @OneToMany(mappedBy = "menuGroup", fetch = FetchType.LAZY)
    private List<Menu> menus;
}
