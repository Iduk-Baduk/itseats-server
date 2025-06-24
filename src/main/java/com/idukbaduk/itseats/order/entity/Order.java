package com.idukbaduk.itseats.order.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import org.locationtech.jts.geom.Point;
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

    @Column(name = "order_received_time", nullable = false)
    private LocalDateTime orderReceivedTime;

    @Column(name = "cook_start_time", nullable = false)
    private LocalDateTime cookStartTime;

    @Column(name = "order_end_time")
    private LocalDateTime orderEndTime;

    public void updateOrderStatusAccept(Rider rider, OrderStatus orderStatus) {
      if (this.orderStatus != OrderStatus.COOKED) {
          throw new OrderException(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL);
      }
      if (this.rider != null) {
          throw new OrderException(OrderErrorCode.ORDER_ALREADY_ASSIGNED);
      }

      this.rider = rider;
      this.orderStatus = orderStatus;
    }
}
