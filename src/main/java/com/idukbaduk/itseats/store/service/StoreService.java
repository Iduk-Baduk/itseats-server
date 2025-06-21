package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.StoreDto;
import com.idukbaduk.itseats.store.dto.StoreListResponse;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final ReviewRepository reviewRepository;

    public Store getStore(Member member, Long storeId) {
        return storeRepository.findByMemberAndStoreId(member, storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public StoreListResponse getAllStores() {

        List<Store> stores = storeRepository.findAllByDeletedFalse();

        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();

        List<StoreImage> images = storeImageRepository.findImagesByStoreIds(storeIds);

        Map<Long, String> storeIdToImageUrl = new HashMap<>();
        for (StoreImage image : images) {
            Long storeId = image.getStore().getStoreId();
            if (!storeIdToImageUrl.containsKey(storeId)) {
                storeIdToImageUrl.put(storeId, image.getImageUrl());
            }
        }

        List<Object[]> stats = reviewRepository.findReviewStatsByStoreIds(storeIds);
        Map<Long, Double> storeIdToAvg = new HashMap<>();
        Map<Long, Integer> storeIdToCount = new HashMap<>();
        for (Object[] row : stats) {
            Long storeId = (Long) row[0];
            Double avg = row[1] != null ? Math.round(((Double) row[1]) * 10) / 10.0 : 0.0;
            Long count = (Long) row[2];
            storeIdToAvg.put(storeId, avg);
            storeIdToCount.put(storeId, count.intValue());
        }

        List<StoreDto> storeDtos = stores.stream()
                .map(store -> StoreDto.builder()
                        .imageUrl(storeIdToImageUrl.get(store.getStoreId()))
                        .name(store.getStoreName())
                        .review(storeIdToAvg.getOrDefault(store.getStoreId(), 0.0))
                        .reviewCount(storeIdToCount.getOrDefault(store.getStoreId(), 0))
                        .build())
                .toList();

        return StoreListResponse.builder()
                .stores(storeDtos)
                .build();
    }
}
