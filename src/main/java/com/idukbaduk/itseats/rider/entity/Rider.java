package com.idukbaduk.itseats.rider.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.rider.entity.enums.DeliveryMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rider")
public class Rider extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rider_id")
    private Long riderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "is_working", nullable = false)
    private Boolean isWorking;

    @Column(name = "location", columnDefinition = "POINT")
    private Point location;

    @Column(name = "preferred_area", nullable = false)
    private String preferredArea;

    public void modifyIsWorking(Boolean isWorking) {
        this.isWorking = isWorking;
    }

    public void updateLocation(Point location) {
        this.location = location;
    }
}
