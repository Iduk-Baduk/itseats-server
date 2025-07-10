package com.idukbaduk.itseats.memberaddress.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
  name = "member_address",
  indexes = @Index(name = "idx_member_address_location", columnList = "location")
)
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

    @Column(name = "location", columnDefinition = "POINT SRID 4326", nullable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_category", nullable = false)
    private AddressCategory addressCategory;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

    public void updateAddress(String mainAddress, String detailAddress, Point location, AddressCategory category) {
        validateAddressFields(mainAddress, detailAddress, location, category);

        this.mainAddress = mainAddress;
        this.detailAddress = detailAddress;
        this.location = location;
        this.addressCategory = category;
        this.lastUsedDate = LocalDateTime.now();
    }

    private void validateAddressFields(String mainAddress, String detailAddress, Point location, AddressCategory category) {
        if (mainAddress == null || mainAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("메인 주소는 필수입니다");
        }
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("상세 주소는 필수입니다");
        }
        if (location == null || category == null) {
            throw new IllegalArgumentException("위치 정보와 주소 카테고리는 필수입니다");
        }
    }
}
