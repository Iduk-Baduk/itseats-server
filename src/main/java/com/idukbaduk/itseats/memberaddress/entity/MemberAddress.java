package com.idukbaduk.itseats.memberaddress.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "member_address")
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "main_address")
    private String mainAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "location")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_category")
    private AddressCategory addressCategory;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

}
