package com.idukbaduk.itseats.memberaddress.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "main_address", nullable = false)
    private String mainAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "location", nullable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_category", nullable = false)
    private AddressCategory addressCategory;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

}
