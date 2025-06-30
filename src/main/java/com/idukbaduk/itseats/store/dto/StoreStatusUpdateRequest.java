package com.idukbaduk.itseats.store.dto;

import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusUpdateRequest {
    private BusinessStatus businessStatus;
    private StoreStatus storeStatus;
    private Boolean orderable;
}
