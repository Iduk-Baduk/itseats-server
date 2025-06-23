package com.idukbaduk.itseats.order.entity.enums;

public enum OrderStatus {

    // 주문 취소
    CANCELED,
    // 주문 접수 중
    WAITING,
    // 주문 조리 (주문 접수)
    COOKING,
    // 조리 완료 (라이더 배차 시작)
    COOKED,
    // 배차 완료
    RIDER_READY,
    // 배달 시작
    DELIVERING,
    // 배달 완료
    DELIVERED,
    // 주문 완료
    COMPLETED
}
