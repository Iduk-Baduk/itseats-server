package com.idukbaduk.itseats.coupon.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_coupon")
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long memberCouponId;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "valid_date")
    private LocalDateTime validDate;
}
