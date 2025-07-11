package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorePauseResponse {
    private Long storeId;
    private boolean orderable;
    private int pauseTime;
    private String restartTime;
}
