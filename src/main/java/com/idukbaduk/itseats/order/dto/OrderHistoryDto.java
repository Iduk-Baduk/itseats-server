package com.idukbaduk.itseats.order.dto;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderHistoryDto {
    private Long orderId;
    private Long storeId;
    private String orderNumber;
    private String storeName;
    private LocalDateTime createdAt;
    private String orderStatus;
    private Integer orderPrice;
    private String deliveryAddress;
    private String deliveryRequest;
    private String menuSummary;
    private String storeImage;

    public static OrderHistoryDto of(Order order, StoreImage storeImage) {
        if (order == null)
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        if (order.getStore() == null)
            throw new StoreException(StoreErrorCode.STORE_NOT_FOUND);

        return builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getStoreId())
                .orderNumber(order.getOrderNumber())
                .storeName(order.getStore().getStoreName())
                .createdAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())
                .orderPrice(order.getOrderPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryRequest(order.getPayment() != null ? order.getPayment().getRiderRequest() : "")
                .menuSummary(
                        order.getOrderMenus() != null ?
                        order.getOrderMenus().stream()
                                .map(OrderMenu::getMenuName)
                                .collect(Collectors.joining(", ")) :
                        "메뉴 정보 없음"
                )
                .storeImage(storeImage == null ? null : storeImage.getImageUrl())
                .build();
    }
}
