package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.FavoriteRepository;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.review.dto.StoreReviewStats;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.review.service.ReviewStatsService;
import com.idukbaduk.itseats.store.dto.*;
import com.idukbaduk.itseats.store.dto.enums.StoreSortOption;
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
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MenuImageRepository menuImageRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final ReviewStatsService reviewStatsService;

    public Store getStore(Member member, Long storeId) {
        return storeRepository.findByMemberAndStoreId(member, storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public StoreListResponse getAllStores(Pageable pageable) {

        // 기본 정렬 무시
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        Slice<Store> stores = storeRepository.findAllOrderByOrderCount(pageRequest);

        return getStoreListResponse(stores, pageable);
    }

    @Transactional(readOnly = true)
    public StoreCategoryListResponse getStoresByCategory(String username, String categoryCode, Pageable pageable,
                                                         StoreSortOption sort, Long addressId) {

        StoreCategory category = storeCategoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));
        Long categoryId = category.getStoreCategoryId();

        // 기본 정렬 무시
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        Slice<Store> stores = switch(sort) {
            case DISTANCE -> {
                Member member = memberRepository.findByUsername(username).orElse(null);
                Point myLocation = getMyLocation(addressId, member); // 존재하지 않을시 기본 값: 서울시청
                yield storeRepository.findNearByStoresByCategory(categoryId, GeoUtil.toString(myLocation), pageRequest);
            }
            case RATING -> storeRepository.findStoresOrderByRating(categoryId, pageRequest);
            case ORDER_COUNT -> storeRepository.findStoresOrderByOrderCount(categoryId, pageRequest);
            case RECENT -> storeRepository.findStoresOrderByCreatedAt(categoryId, pageRequest);
        };

        if (stores == null || stores.getContent().isEmpty()) {
            return StoreCategoryListResponse.builder()
                    .category(categoryCode)
                    .categoryName(category.getCategoryName())
                    .stores(Collections.emptyList())
                    .currentPage(pageable.getPageNumber())
                    .hasNext(false)
                    .build();
        }

        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();
        Map<Long, List<String>> storeIdToImages = buildStoreImageMap(storeIds);
        Map<Long, StoreReviewStats> reviewStatsMap = reviewStatsService.getReviewStatsForStores(storeIds);

        List<StoreDto> storeDtos = buildStoreDtos(stores.getContent(), storeIdToImages, reviewStatsMap);
        return StoreCategoryListResponse.builder()
                .category(categoryCode)
                .categoryName(category.getCategoryName())
                .stores(storeDtos)
                .currentPage(pageable.getPageNumber())
                .hasNext(stores.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public StoreListResponse searchStores(String username, String keyword, Pageable pageable, StoreSortOption sort,
                                          Long addressId) {
        if (keyword == null || keyword.isEmpty()) {
            return StoreListResponse.builder()
                    .stores(Collections.emptyList())
                    .currentPage(pageable.getPageNumber())
                    .hasNext(false)
                    .build();
        }

        // 기본 정렬 무시
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        Slice<Store> stores = switch(sort) {
            case DISTANCE -> {
                Member member = memberRepository.findByUsername(username).orElse(null);
                Point myLocation = getMyLocation(addressId, member); // 존재하지 않을시 기본 값: 서울시청
                yield storeRepository.searchNearByStores(keyword, GeoUtil.toString(myLocation), pageRequest);
            }
            case RATING -> storeRepository.searchStoresOrderByRating(keyword, pageRequest);
            case ORDER_COUNT -> storeRepository.searchStoresOrderByOrderCount(keyword, pageRequest);
            case RECENT -> storeRepository.searchStoresOrderByCreatedAt(keyword, pageRequest);
        };

        return getStoreListResponse(stores, pageable);
    }

    private StoreListResponse getStoreListResponse(Slice<Store> stores, Pageable pageable) {
        if (stores == null || stores.getContent().isEmpty()) {
            return StoreListResponse.builder()
                    .stores(Collections.emptyList())
                    .currentPage(pageable.getPageNumber())
                    .hasNext(false)
                    .build();
        }

        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();
        Map<Long, List<String>> storeIdToImages = buildStoreImageMap(storeIds);

        Map<Long, StoreReviewStats> reviewStatsMap = reviewStatsService.getReviewStatsForStores(storeIds);

        List<StoreDto> storeDtos = buildStoreDtos(stores.getContent(), storeIdToImages, reviewStatsMap);

        return StoreListResponse.builder()
                .stores(storeDtos)
                .currentPage(pageable.getPageNumber())
                .hasNext(stores.hasNext())
                .build();
    }

    private Point getMyLocation(Long addressId, Member member) {
        return Optional.ofNullable(member)
                .map(m -> memberAddressRepository.findByMemberAndAddressId(m, addressId)
                        .orElseThrow(() -> new MemberAddressException(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND))
                        .getLocation())
                .orElse(GeoUtil.toPoint(126.9779451, 37.5662952));  // 서울시청 (기본값)
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

    private List<StoreDto> buildStoreDtos(List<Store> stores,
                                          Map<Long, List<String>> imageMap,
                                          Map<Long, StoreReviewStats> reviewStatsMap) {
        return stores.stream()
                .map(store -> {
                    StoreReviewStats stats = reviewStatsMap.getOrDefault(store.getStoreId(),
                            new StoreReviewStats(0.0, 0));
                    return StoreDto.builder()
                            .storeId(store.getStoreId())
                            .name(store.getStoreName())
                            .review(stats.avg())
                            .reviewCount(stats.count())
                            .images(imageMap.getOrDefault(store.getStoreId(), Collections.emptyList()))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(String username, Long storeId) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Store store = storeRepository.findByStoreIdAndDeletedFalse(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        boolean isLiked = favoriteRepository.existsByMemberAndStore(member, store);

        StoreReviewStats reviewStats = reviewStatsService.getReviewStats(storeId);

        List<String> images = storeImageRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId)
                .stream()
                .map(StoreImage::getImageUrl)
                .toList();

        return StoreDetailResponse.builder()
                .name(store.getStoreName())
                .isLiked(isLiked)
                .review(reviewStats.avg())
                .reviewCount(reviewStats.count())
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
