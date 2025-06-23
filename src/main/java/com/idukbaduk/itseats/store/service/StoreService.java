package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.FavoriteRepository;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.StoreDetailResponse;
import com.idukbaduk.itseats.store.dto.StoreCategoryListResponse;
import com.idukbaduk.itseats.store.dto.StoreDto;
import com.idukbaduk.itseats.store.dto.StoreListResponse;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberService memberService;
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

        Map<Long, String> storeIdToImageUrl = buildStoreImageMap(storeIds);

        Map<Long, Double> storeIdToAvg = new HashMap<>();
        Map<Long, Integer> storeIdToCount = new HashMap<>();
        buildReviewStatsMap(storeIds, storeIdToAvg, storeIdToCount);

        List<StoreDto> storeDtos = buildStoreDtos(stores, storeIdToImageUrl, storeIdToAvg, storeIdToCount);

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

        Map<Long, String> storeIdToImageUrl = buildStoreImageMap(storeIds);

        Map<Long, Double> storeIdToAvg = new HashMap<>();
        Map<Long, Integer> storeIdToCount = new HashMap<>();
        buildReviewStatsMap(storeIds, storeIdToAvg, storeIdToCount);

        List<StoreDto> storeDtos = buildStoreDtos(stores, storeIdToImageUrl, storeIdToAvg, storeIdToCount);

        return StoreCategoryListResponse.builder()
                .category(categoryCode)
                .categoryName(category.getCategoryName())
                .stores(storeDtos)
                .build();
    }

    private Map<Long, String> buildStoreImageMap(List<Long> storeIds) {
        List<StoreImage> images = storeImageRepository.findImagesByStoreIds(storeIds);
        Map<Long, String> storeIdToImageUrl = new HashMap<>();
        for (StoreImage image : images) {
            Long storeId = image.getStore().getStoreId();
            if (!storeIdToImageUrl.containsKey(storeId)) {
                storeIdToImageUrl.put(storeId, image.getImageUrl());
            }
        }
        return storeIdToImageUrl;
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
                                          Map<Long, String> imageMap,
                                          Map<Long, Double> avgMap,
                                          Map<Long, Integer> countMap) {
        return stores.stream()
                .map(store -> StoreDto.builder()
                        .imageUrl(imageMap.get(store.getStoreId()))
                        .name(store.getStoreName())
                        .review(avgMap.getOrDefault(store.getStoreId(), 0.0))
                        .reviewCount(countMap.getOrDefault(store.getStoreId(), 0))
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
                .build();
    }

}
