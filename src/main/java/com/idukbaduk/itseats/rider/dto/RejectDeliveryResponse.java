package com.idukbaduk.itseats.rider.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RejectDeliveryResponse {

    private String rejectReason;
}
