package com.idukbaduk.itseats.menu.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "menu_option")
public class MenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opt_group_id", nullable = false)
    private MenuOptionGroup menuOptionGroup;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "option_price", nullable = false)
    private Long optionPrice;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "option_status", nullable = false)
    private MenuStatus optionStatus;

    @Column(name = "option_priority", nullable = false)
    private int optionPriority;
}
