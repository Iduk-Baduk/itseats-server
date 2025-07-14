package com.idukbaduk.itseats.order.dto;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.store.dto.PointDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RiderOrderDetailsResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private String orderTime;
    private int totalPrice;
    private int deliveryFee;
    private List<OrderItemDTO> orderItems;
    private String storePhone;
    private String memberPhone;
    private String address;
    private PointDto destination;
    private String storeRequest;
    private String riderRequest;

    public static RiderOrderDetailsResponse of(Order order, Payment payment) {

        return RiderOrderDetailsResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus().name())
                .orderTime(order.getCreatedAt().toString())
                .totalPrice(order.getOrderPrice())
                .deliveryFee(order.getDeliveryFee())
                .orderItems(order.getOrderMenus().stream().map(OrderItemDTO::of).toList())
                .storePhone(order.getStore().getStorePhone())
                .memberPhone(order.getMember().getPhone())
                .address(order.getDeliveryAddress())
                .destination(order.getDestinationLocation() == null ? null : new PointDto(order.getDestinationLocation()))
                .storeRequest(payment.getStoreRequest())
                .riderRequest(payment.getRiderRequest())
                .build();
    }
}
