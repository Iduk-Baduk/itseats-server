package com.idukbaduk.itseats.order.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.Point;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "order_price", nullable = false)
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "delivery_eta", nullable = false)
    private LocalDateTime deliveryEta;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "destination_location", nullable = false)
    private Point destinationLocation;

    @Column(name = "store_location", nullable = false)
    private Point storeLocation;
}
