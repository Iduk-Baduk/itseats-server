package com.idukbaduk.itseats.order.entity;

import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.awt.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "order_price")
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "delivery_eta")
    private LocalDateTime deliveryEta;

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "destination_location")
    private Point destinationLocation;

    @Column(name = "store_location")
    private Point storeLocation;
}
