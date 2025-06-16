package com.idukbaduk.itseats.menu.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
