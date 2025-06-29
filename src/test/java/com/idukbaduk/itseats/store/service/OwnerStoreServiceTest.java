package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.FranchiseRepository;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerStoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StoreMediaService storeMediaService;

    @InjectMocks
    private OwnerStoreService ownerStoreService;

    @Test
    @DisplayName("가게 등록 성공")
    void createStore_success() {
        // given
        String username = "testuser";
        StoreCreateRequest request = StoreCreateRequest.builder()
                .name("테스트가게")
                .description("설명")
                .address("서울시 강남구")
                .lng(127.0)
                .lat(37.5)
                .phone("010-1234-5678")
                .isFranchise(true)
                .categoryName("한식")
                .franchiseId(1L)
                .defaultDeliveryFee(3000)
                .onlyOneDeliveryFee(1000)
                .build();
        List<MultipartFile> images = List.of(mock(MultipartFile.class));

        Member member = Member.builder().memberId(1L).build();
        StoreCategory category = StoreCategory.builder().storeCategoryId(2L).categoryName("한식").build();
        Franchise franchise = Franchise.builder().franchiseId(1L).brandName("BBQ").build();
        Store store = Store.builder().storeId(10L).storeName("테스트가게").build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(storeCategoryRepository.findByCategoryName("한식")).thenReturn(Optional.of(category));
        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));
        when(storeRepository.save(any(Store.class))).thenReturn(store);

        // when
        StoreCreateResponse response = ownerStoreService.createStore(username, request, images);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStoreId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("테스트가게");
        assertThat(response.getCategoryName()).isEqualTo("한식");
        assertThat(response.isFranchise()).isTrue();

        verify(storeMediaService).createStoreImages(any(Store.class), eq(images));
    }

    @Test
    @DisplayName("카테고리가 없으면 예외 발생")
    void createStore_categoryNotFound() {
        // given
        String username = "testuser";
        StoreCreateRequest request = StoreCreateRequest.builder()
                .categoryName("없는카테고리")
                .isFranchise(false)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(Member.builder().build()));
        when(storeCategoryRepository.findByCategoryName("없는카테고리")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerStoreService.createStore(username, request, Collections.emptyList()))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프랜차이즈 매장인데 franchiseId가 없으면 예외 발생")
    void createStore_franchiseIdRequired() {
        // given
        String username = "testuser";
        StoreCreateRequest request = StoreCreateRequest.builder()
                .categoryName("한식")
                .isFranchise(true)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(Member.builder().build()));
        when(storeCategoryRepository.findByCategoryName("한식")).thenReturn(Optional.of(StoreCategory.builder().build()));

        // when & then
        assertThatThrownBy(() -> ownerStoreService.createStore(username, request, Collections.emptyList()))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.FRANCHISE_ID_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("프랜차이즈 ID가 잘못된 경우 예외 발생")
    void createStore_franchiseNotFound() {
        // given
        String username = "testuser";
        StoreCreateRequest request = StoreCreateRequest.builder()
                .categoryName("한식")
                .isFranchise(true)
                .franchiseId(99L)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(Member.builder().build()));
        when(storeCategoryRepository.findByCategoryName("한식")).thenReturn(Optional.of(StoreCategory.builder().build()));
        when(franchiseRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerStoreService.createStore(username, request, Collections.emptyList()))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.FRANCHISE_NOT_FOUND.getMessage());
    }
}
