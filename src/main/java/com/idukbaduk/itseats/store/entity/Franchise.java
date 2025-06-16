package com.idukbaduk.itseats.store.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "franchise")
public class Franchise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "franchise_id")
    private Long franchiseId;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "brand_image_url", nullable = false)
    private String brandImageUrl;
}
