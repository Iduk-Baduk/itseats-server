package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.enums.TargetType;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerCouponService {

    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public StoreCouponCreateResponse createStoreCoupon(Long storeId, CouponCreateRequest request, String username) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getUsername().equals(username)) {
            throw new StoreException(StoreErrorCode.NOT_STORE_OWNER);
        }

        Coupon coupon = Coupon.builder()
                .store(store)
                .couponName(request.getName())
                .description(request.getDescription())
                .discountValue(request.getDiscountValue())
                .couponType(request.getCouponType())
                .quantity(request.getQuantity())
                .minPrice(request.getMinPrice())
                .issueStartDate(request.getIssueStartDate())
                .validDate(request.getValidDate())
                .targetType(TargetType.STORE)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        return StoreCouponCreateResponse.builder()
                .couponId(savedCoupon.getCouponId())
                .name(savedCoupon.getCouponName())
                .quantity(savedCoupon.getQuantity())
                .couponType(savedCoupon.getCouponType())
                .minPrice(savedCoupon.getMinPrice())
                .discountValue(savedCoupon.getDiscountValue())
                .issueStartDate(savedCoupon.getIssueStartDate())
                .validDate(savedCoupon.getValidDate())
                .build();
    }
}
