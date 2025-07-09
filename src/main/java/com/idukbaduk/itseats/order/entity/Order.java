package com.idukbaduk.itseats.order.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "order_price", nullable = false)
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @Column(name = "delivery_eta", nullable = false)
    private LocalDateTime deliveryEta;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "destination_location", columnDefinition = "POINT", nullable = false)
    private Point destinationLocation;

    @Column(name = "store_location", columnDefinition = "POINT", nullable = false)
    private Point storeLocation;

    @Column(name = "order_received_time")
    private LocalDateTime orderReceivedTime;

    @Column(name = "cook_start_time")
    private LocalDateTime cookStartTime;

    @Column(name = "order_end_time")
    private LocalDateTime orderEndTime;

    @Column(name = "reject_reason")
    private String rejectReason;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderMenu> orderMenus;

    @OneToOne(mappedBy = "order")
    private Payment payment;

    public void updateOrderStatusAccept(Rider rider, OrderStatus orderStatus) {
        if (this.rider != null) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_ASSIGNED);
        }
        orderStatus.validateTransitionFrom(this.orderStatus);

        this.rider = rider;
        this.orderStatus = orderStatus;
    }

    public void updateStatus(OrderStatus orderStatus) {
        orderStatus.validateTransitionFrom(this.orderStatus);
        this.orderStatus = orderStatus;
        this.cookStartTime = LocalDateTime.now();
    }

    public void reject(String reason) {
        OrderStatus.REJECTED.validateTransitionFrom(this.orderStatus);
        this.orderStatus = OrderStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void updateDeliveryEta(LocalDateTime deliveryEta) {
        this.deliveryEta = deliveryEta;
    }
}
