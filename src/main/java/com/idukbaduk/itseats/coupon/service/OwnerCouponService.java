package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.FranchiseCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.enums.TargetType;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.FranchiseRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerCouponService {

    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;
    private final FranchiseRepository franchiseRepository;

    @Transactional
    public StoreCouponCreateResponse createStoreCoupon(Long storeId, CouponCreateRequest request, String username) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getUsername().equals(username)) {
            throw new StoreException(StoreErrorCode.NOT_STORE_OWNER);
        }

        if (request.getIssueEndDate().isBefore(request.getIssueStartDate())) {
            throw new CouponException(CouponErrorCode.INVALID_DATE_RANGE);
        }
        if (request.getValidDate().isBefore(request.getIssueEndDate())) {
            throw new CouponException(CouponErrorCode.INVALID_DATE_RANGE);
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
                .issueEndDate(request.getIssueEndDate())
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
                .issueEndDate(savedCoupon.getIssueEndDate())
                .validDate(savedCoupon.getValidDate())
                .build();
    }

    @Transactional
    public FranchiseCouponCreateResponse createFranchiseCoupon(Long franchiseId, CouponCreateRequest request) {

        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.FRANCHISE_NOT_FOUND));

        if (request.getIssueEndDate().isBefore(request.getIssueStartDate())) {
            throw new CouponException(CouponErrorCode.INVALID_DATE_RANGE);
        }
        if (request.getValidDate().isBefore(request.getIssueEndDate())) {
            throw new CouponException(CouponErrorCode.INVALID_DATE_RANGE);
        }

        Coupon coupon = Coupon.builder()
                .franchise(franchise)
                .couponName(request.getName())
                .description(request.getDescription())
                .discountValue(request.getDiscountValue())
                .couponType(request.getCouponType())
                .quantity(request.getQuantity())
                .minPrice(request.getMinPrice())
                .issueStartDate(request.getIssueStartDate())
                .issueEndDate(request.getIssueEndDate())
                .validDate(request.getValidDate())
                .targetType(TargetType.FRANCHISE)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        return FranchiseCouponCreateResponse.builder()
                .franchiseName(franchise.getBrandName())
                .couponId(savedCoupon.getCouponId())
                .name(savedCoupon.getCouponName())
                .quantity(savedCoupon.getQuantity())
                .couponType(savedCoupon.getCouponType())
                .minPrice(savedCoupon.getMinPrice())
                .discountValue(savedCoupon.getDiscountValue())
                .issueStartDate(savedCoupon.getIssueStartDate())
                .issueEndDate(savedCoupon.getIssueEndDate())
                .validDate(savedCoupon.getValidDate())
                .build();
    }
}
