package com.idukbaduk.itseats.rider.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.rider.entity.enums.SettlementStatus;
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
@Table(name = "rider_settlement")
public class RiderSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rider_settlement_id")
    private Long riderSettlementId;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;

    @Column(name = "total_delivery_count", nullable = false)
    private int totalDeliveryCount;

    @Column(name = "total_earning", nullable = false)
    private Long totalEarning;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.UNSETTLED;
}
