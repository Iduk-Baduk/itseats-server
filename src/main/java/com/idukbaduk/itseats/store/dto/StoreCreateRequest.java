package com.idukbaduk.itseats.store.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {
    private String name;
    private String description;
    private String address;
    private double lng; // 경도
    private double lat; // 위도
    private String phone;
    private boolean isFranchise;
    private String categoryName;
    private Long franchiseId;
    private int defaultDeliveryFee;
    private int onlyOneDeliveryFee;
}
