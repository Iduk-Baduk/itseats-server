package com.idukbaduk.itseats.store.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "store_image")
public class StoreImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_image_id")
    private Long storeImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
