package com.idukbaduk.itseats.order.entity.enums;

import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    // 주문 취소
    CANCELED(null),
    // 주문 접수 중
    WAITING(null),
    // 주문 수락
    ACCEPTED(WAITING),
    // 주문 거절
    REJECTED(WAITING),
    // 주문 조리
    COOKING(ACCEPTED),
    // 조리 완료 (라이더 배차 시작)
    COOKED(COOKING),
    // 배차 완료
    RIDER_READY(COOKED),
    // 매장 도착
    ARRIVED(RIDER_READY),
    // 배달 시작
    DELIVERING(RIDER_READY),
    // 배달 완료
    DELIVERED(DELIVERING),
    // 주문 완료
    COMPLETED(DELIVERED);

    private final OrderStatus previousStatus;

    public void validateTransitionFrom(OrderStatus currentStatus) {
        if (!Objects.equals(this.previousStatus, currentStatus)) {
            throw new OrderException(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL);
        }
    }
}
