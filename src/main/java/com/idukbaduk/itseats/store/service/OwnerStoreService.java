package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.*;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.FranchiseRepository;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerStoreService {

    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final FranchiseRepository franchiseRepository;
    private final StoreImageRepository storeImageRepository;
    private final MemberRepository memberRepository;
    private final StoreMediaService storeMediaService;

    @Transactional(readOnly = true)
    public StoreDashboardResponse getDashboard(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
        double customerRating = avgRating != null ? avgRating : 0.0;

        Double avgCookTime = orderRepository.findAverageCookTimeByStoreId(storeId);
        Long accurateCount = orderRepository.countAccurateOrdersByStoreId(storeId);
        Double avgPickupTime = orderRepository.findAveragePickupTimeByStoreId(storeId);
        Long totalOrders = orderRepository.countTotalOrdersByStoreId(storeId);
        Long acceptedOrders = orderRepository.countAcceptedOrdersByStoreId(storeId);

        double cookTimeAccuracy = (totalOrders != null && totalOrders > 0 && accurateCount != null)
                ? accurateCount * 100.0 / totalOrders : 0.0;

        double acceptanceRate = (totalOrders != null && totalOrders > 0 && acceptedOrders != null)
                ? acceptedOrders * 100.0 / totalOrders : 0.0;

        return StoreDashboardResponse.builder()
                .storeName(store.getStoreName())
                .customerRating(customerRating)
                .avgCookTime(Math.round(avgCookTime) + "분")
                .cookTimeAccuracy(Math.round(cookTimeAccuracy) + "%")
                .pickupTime(Math.round(avgPickupTime) + "분")
                .orderAcceptanceRate(Math.round(acceptanceRate) + "%")
                .build();
    }

    @Transactional
    public StoreCreateResponse createStore(String username, StoreCreateRequest request, List<MultipartFile> images) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        StoreCategory storeCategory = storeCategoryRepository.
                findByCategoryName(request.getCategoryName())
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        Franchise franchise = null;
        if (request.isFranchise()) {
            if (request.getFranchiseId() == null) {
                throw new StoreException(StoreErrorCode.FRANCHISE_ID_REQUIRED);
            }
            franchise = franchiseRepository.findById(request.getFranchiseId())
                    .orElseThrow(() -> new StoreException(StoreErrorCode.FRANCHISE_NOT_FOUND));
        }

        Store store = Store.builder()
                .member(member)
                .storeCategory(storeCategory)
                .franchise(franchise)
                .storeName(request.getName())
                .description(request.getDescription())
                .storeAddress(request.getAddress())
                .location(GeoUtil.toPoint(request.getLng(), request.getLat()))
                .storePhone(request.getPhone())
                .defaultDeliveryFee(request.getDefaultDeliveryFee())
                .onlyOneDeliveryFee(request.getOnlyOneDeliveryFee())
                .businessStatus(BusinessStatus.OPEN)
                .storeStatus(StoreStatus.ACCEPTED) // 관리자 기능 개발전 임시로 ACCEPTED 설정
                .orderable(false)
                .build();

        Store savedStore = storeRepository.save(store);

        storeMediaService.createStoreImages(savedStore, images);

        return StoreCreateResponse.builder()
                .storeId(savedStore.getStoreId())
                .name(savedStore.getStoreName())
                .categoryName(storeCategory.getCategoryName())
                .isFranchise(request.isFranchise())
                .description(savedStore.getDescription())
                .address(savedStore.getStoreAddress())
                .phone(savedStore.getStorePhone())
                .build();
    }

    @Transactional
    public void updateStatus(Long storeId, StoreStatusUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        if (request.getBusinessStatus() != null) {
            store.updateBusinessStatus(request.getBusinessStatus());
        }
        if (request.getStoreStatus() != null) {
            store.updateStoreStatus(request.getStoreStatus());
        }
        if (request.getOrderable() != null) {
            store.updateOrderable(request.getOrderable());
        }
    }

    @Transactional
    public StorePauseResponse pauseOrder(Long storeId, int pauseTime) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        store.updateOrderable(false);

        LocalDateTime restartTime = LocalDateTime.now().plusMinutes(pauseTime);
        String restartTimeStr = restartTime.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"));

        return StorePauseResponse.builder()
                .storeId(store.getStoreId())
                .orderable(store.getOrderable())
                .pauseTime(pauseTime)
                .restartTime(restartTimeStr)
                .build();
    }
    
    @Transactional
    public void restartOrder(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        store.updateOrderable(true);
    }
}
