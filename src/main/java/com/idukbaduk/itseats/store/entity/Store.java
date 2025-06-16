package com.idukbaduk.itseats.store.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.geo.Point;

@Entity
@Getter
@Table(name = "store")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_category_id", nullable = false)
    private StoreCategory storeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id")
    private Franchise franchise;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "store_address", nullable = false)
    private String storeAddress;

    @Column(name = "location", nullable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_status", nullable = false)
    private BusinessStatus businessStatus;

    @Column(name = "store_phone", nullable = false)
    private String storePhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false)
    private StoreStatus storeStatus;

    @Column(name = "orderable", nullable = false)
    private Boolean orderable = true;
}
