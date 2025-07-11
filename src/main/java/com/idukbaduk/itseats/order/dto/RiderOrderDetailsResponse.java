package com.idukbaduk.itseats.order.dto;

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
    private List<OrderItemDTO> orderItems;
    private String storePhone;
    private String memberPhone;
    private String storeRequest;
    private String riderRequest;
}
