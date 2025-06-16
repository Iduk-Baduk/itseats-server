package com.idukbaduk.itseats.order.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "order_menu")
public class OrderMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_menu_id")
    private Long orderMenuId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_option")
    private String menuOption;
}
