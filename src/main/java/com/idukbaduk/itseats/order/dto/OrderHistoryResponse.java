package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderHistoryResponse {
    List<OrderHistoryDto> orders;
    Boolean hasNext;
}
