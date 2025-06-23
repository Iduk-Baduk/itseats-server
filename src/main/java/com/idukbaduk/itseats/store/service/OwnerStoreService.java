package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.FranchiseRepository;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerStoreService {

    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final FranchiseRepository franchiseRepository;
    private final StoreImageRepository storeImageRepository;
    private final MemberRepository memberRepository;
    private final StoreMediaService storeMediaService;

    @Transactional
    public StoreCreateResponse createStore(String username, StoreCreateRequest request) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(()-> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

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

        Point point = new Point(request.getLocationX(), request.getLocationY());

        Store store = Store.builder()
                .member(member)
                .storeCategory(storeCategory)
                .franchise(franchise)
                .storeName(request.getName())
                .description(request.getDescription())
                .storeAddress(request.getAddress())
                .location(point)
                .storePhone(request.getPhone())
                .defaultDeliveryFee(request.getDefaultDeliveryFee())
                .onlyOneDeliveryFee(request.getOnlyOneDeliveryFee())
                .build();

        Store savedStore = storeRepository.save(store);

        storeMediaService.createStoreImages(savedStore, request.getImages());

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
}
