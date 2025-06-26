package com.idukbaduk.itseats.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRejectResponse {
    private boolean success;
    private String reason;
}
