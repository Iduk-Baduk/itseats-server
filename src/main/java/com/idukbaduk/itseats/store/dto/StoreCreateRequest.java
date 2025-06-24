package com.idukbaduk.itseats.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private List<MultipartFile> images;
    private boolean isFranchise;
    private String categoryName;
    private Long franchiseId;
    private int defaultDeliveryFee;
    private int onlyOneDeliveryFee;
}
