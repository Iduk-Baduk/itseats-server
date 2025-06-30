package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.FavoriteRepository;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.*;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuImageRepository menuImageRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final StoreCategoryRepository storeCategoryRepository;


    public Store getStore(Member member, Long storeId) {
        return storeRepository.findByMemberAndStoreId(member, storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public StoreListResponse getAllStores() {

        List<Store> stores = storeRepository.findAllByDeletedFalse();

        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();

        Map<Long, List<String>> storeIdToImages = buildStoreImageMap(storeIds);

        Map<Long, Double> storeIdToAvg = new HashMap<>();
        Map<Long, Integer> storeIdToCount = new HashMap<>();
        buildReviewStatsMap(storeIds, storeIdToAvg, storeIdToCount);

        List<StoreDto> storeDtos = buildStoreDtos(stores, storeIdToImages, storeIdToAvg, storeIdToCount);

        return StoreListResponse.builder()
                .stores(storeDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public StoreCategoryListResponse getStoresByCategory(String categoryCode) {

        StoreCategory category = storeCategoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        List<Store> stores = storeRepository.findAllByStoreCategory_CategoryCodeAndDeletedFalse(categoryCode);

        if (stores.isEmpty()) {
            return StoreCategoryListResponse.builder()
                    .category(categoryCode)
                    .categoryName(category.getCategoryName())
                    .stores(Collections.emptyList())
                    .build();
        }

        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();

        Map<Long, List<String>> storeIdToImages = buildStoreImageMap(storeIds);

        Map<Long, Double> storeIdToAvg = new HashMap<>();
        Map<Long, Integer> storeIdToCount = new HashMap<>();
        buildReviewStatsMap(storeIds, storeIdToAvg, storeIdToCount);

        List<StoreDto> storeDtos = buildStoreDtos(stores, storeIdToImages, storeIdToAvg, storeIdToCount);

        return StoreCategoryListResponse.builder()
                .category(categoryCode)
                .categoryName(category.getCategoryName())
                .stores(storeDtos)
                .build();
    }

    private Map<Long, List<String>> buildStoreImageMap(List<Long> storeIds) {
        List<StoreImage> images = storeImageRepository.findImagesByStoreIds(storeIds);
        List<MenuImageWithStoreId> menuImages = menuImageRepository.findTop2ImagesPerStoreIds(storeIds);

        Map<Long, List<String>> storeIdToImages = new HashMap<>();
        for (StoreImage image : images) {
            Long storeId = image.getStore().getStoreId();
            storeIdToImages
                    .computeIfAbsent(storeId, k -> new ArrayList<>())
                    .add(image.getImageUrl());
        }
        for (MenuImageWithStoreId image : menuImages) {
            storeIdToImages
                    .computeIfAbsent(image.getStoreId(), k -> new ArrayList<>())
                    .add(image.getImageUrl());
        }

        return storeIdToImages;
    }

    private void buildReviewStatsMap(List<Long> storeIds,
                                     Map<Long, Double> avgMap,
                                     Map<Long, Integer> countMap) {
        List<Object[]> stats = reviewRepository.findReviewStatsByStoreIds(storeIds);
        for (Object[] row : stats) {
            Long storeId = (Long) row[0];
            Double avg = row[1] != null ? Math.round(((Double) row[1]) * 10) / 10.0 : 0.0;
            Long count = (Long) row[2];
            avgMap.put(storeId, avg);
            countMap.put(storeId, count.intValue());
        }
    }

    private List<StoreDto> buildStoreDtos(List<Store> stores,
                                          Map<Long, List<String>> imageMap,
                                          Map<Long, Double> avgMap,
                                          Map<Long, Integer> countMap) {
        return stores.stream()
                .map(store -> StoreDto.builder()
                        .storeId(store.getStoreId())
                        .name(store.getStoreName())
                        .review(avgMap.getOrDefault(store.getStoreId(), 0.0))
                        .reviewCount(countMap.getOrDefault(store.getStoreId(), 0))
                        .images(imageMap.getOrDefault(store.getStoreId(), Collections.emptyList()))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(String username, Long storeId) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Store store = storeRepository.findByStoreIdAndDeletedFalse(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        boolean isLiked = favoriteRepository.existsByMemberAndStore(member, store);

        Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);

        int reviewCount = reviewRepository.countByStoreId(storeId);

        List<String> images = storeImageRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId)
                .stream()
                .map(StoreImage::getImageUrl)
                .toList();

        return StoreDetailResponse.builder()
                .name(store.getStoreName())
                .isLiked(isLiked)
                .review(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0)
                .reviewCount(reviewCount)
                .images(images)
                .description(store.getDescription() != null ? store.getDescription() : "")
                .address(store.getStoreAddress())
                .phone(store.getStorePhone())
                .defaultDeliveryFee(store.getDefaultDeliveryFee())
                .onlyOneDeliveryFee(store.getOnlyOneDeliveryFee())
                .isOpen(store.getBusinessStatus() == BusinessStatus.OPEN)
                .orderable(store.getOrderable())
                .location(new PointDto(store.getLocation()))
                .build();
    }
}
