package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.member.entity.Member;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreImageRepository storeImageRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FavoriteRepository favoriteRepository;
    
    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @Mock
    private MemberRepository memberRepository;


    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("가맹점 정보를 성공적으로 반환")
    void getStore_success() {
        // given
        Long storeId = 1L;
        Member mockMember = Member.builder()
                .memberId(1L)
                .build();
        Store store = Store.builder()
                .storeId(storeId)
                .member(mockMember)
                .build();

        when(storeRepository.findByMemberAndStoreId(mockMember, storeId)).thenReturn(Optional.of(store));

        // when
        Store result = storeService.getStore(mockMember, storeId);

        // then
        assertThat(result.getStoreId()).isEqualTo(storeId);
    }

    @Test
    @DisplayName("존재하지 않는 가맹점 조회시 예외 발생")
    void getStore_notExist() {
        // given
        Long storeId = 1L;
        Member mockMember = Member.builder()
                .memberId(1L)
                .build();
        when(storeRepository.findByMemberAndStoreId(mockMember, storeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStore(mockMember, storeId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("전체 가게 목록 조회 성공")
    void getAllStores_success() {
        // given
        Store store1 = Store.builder().storeId(1L).storeName("버커킹 구름점").build();
        Store store2 = Store.builder().storeId(2L).storeName("맥도날드 구름점").build();
        Store store3 = Store.builder().storeId(3L).storeName("롯데리아 구름점").build();

        StoreImage image1 = StoreImage.builder().store(store1).imageUrl("s3 url 1").build();
        StoreImage image2 = StoreImage.builder().store(store2).imageUrl("s3 url 2").build();
        StoreImage image3 = StoreImage.builder().store(store3).imageUrl("s3 url 3").build();

        when(storeRepository.findAllByDeletedFalse())
                .thenReturn(List.of(store1, store2, store3));
        when(storeImageRepository.findImagesByStoreIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(image1, image2, image3));
        when(reviewRepository.findReviewStatsByStoreIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(
                        new Object[]{1L, 4.9, 1742L},
                        new Object[]{2L, 4.7, 2847L},
                        new Object[]{3L, 4.5, 3715L}
                ));

        // when
        StoreListResponse response = storeService.getAllStores();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStores()).hasSize(3);

        StoreDto dto1 = response.getStores().get(0);
        assertThat(dto1.getImageUrl()).isEqualTo("s3 url 1");
        assertThat(dto1.getName()).isEqualTo("버커킹 구름점");
        assertThat(dto1.getReview()).isEqualTo(4.9);
        assertThat(dto1.getReviewCount()).isEqualTo(1742);

        StoreDto dto2 = response.getStores().get(1);
        assertThat(dto2.getImageUrl()).isEqualTo("s3 url 2");
        assertThat(dto2.getName()).isEqualTo("맥도날드 구름점");
        assertThat(dto2.getReview()).isEqualTo(4.7);
        assertThat(dto2.getReviewCount()).isEqualTo(2847);

        StoreDto dto3 = response.getStores().get(2);
        assertThat(dto3.getImageUrl()).isEqualTo("s3 url 3");
        assertThat(dto3.getName()).isEqualTo("롯데리아 구름점");
        assertThat(dto3.getReview()).isEqualTo(4.5);
        assertThat(dto3.getReviewCount()).isEqualTo(3715);
    }

    @Test
    @DisplayName("리뷰가 없는 가게도 정상 처리")
    void getAllStores_noReviews_success() {
        // given
        Store store1 = Store.builder().storeId(1L).storeName("신규 가게").build();
        StoreImage image1 = StoreImage.builder().store(store1).imageUrl("s3 url").build();

        when(storeRepository.findAllByDeletedFalse()).thenReturn(List.of(store1));
        when(storeImageRepository.findImagesByStoreIds(List.of(1L))).thenReturn(List.of(image1));
        when(reviewRepository.findReviewStatsByStoreIds(List.of(1L))).thenReturn(Collections.emptyList());

        // when
        StoreListResponse response = storeService.getAllStores();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStores()).hasSize(1);

        StoreDto dto = response.getStores().get(0);
        assertThat(dto.getImageUrl()).isEqualTo("s3 url");
        assertThat(dto.getName()).isEqualTo("신규 가게");
        assertThat(dto.getReview()).isEqualTo(0.0);
        assertThat(dto.getReviewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("가게가 없을 때 빈 리스트 반환")
    void getAllStores_emptyList_returnsEmptyList() {
        // given
        when(storeRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

        // when
        StoreListResponse response = storeService.getAllStores();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStores()).isEmpty(); // 빈 배열 확인
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    void getStoreDetail_success() {
        // given
        Long storeId = 1L;

        Member member = Member.builder()
                .memberId(10L)
                .username("testUser")
                .build();

        Store store = Store.builder()
                .storeId(storeId)
                .storeName("스타벅스 구름점")
                .build();

        List<StoreImage> images = List.of(
                StoreImage.builder().store(store).imageUrl("s3_url1").displayOrder(1).build(),
                StoreImage.builder().store(store).imageUrl("s3_url2").displayOrder(2).build()
        );

        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.of(member));
        when(storeRepository.findByStoreIdAndDeletedFalse(storeId)).thenReturn(Optional.of(store));
        when(favoriteRepository.existsByMemberAndStore(member, store)).thenReturn(true);
        when(reviewRepository.findAverageRatingByStoreId(storeId)).thenReturn(4.9);
        when(reviewRepository.countByStoreId(storeId)).thenReturn(13812);
        when(storeImageRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId)).thenReturn(images);

        // when
        StoreDetailResponse response = storeService.getStoreDetail(member.getUsername(), storeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("스타벅스 구름점");
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getReview()).isEqualTo(4.9);
        assertThat(response.getReviewCount()).isEqualTo(13812);
        assertThat(response.getImages()).containsExactly("s3_url1", "s3_url2");
    }

    @Test
    @DisplayName("가게 상세 정보 조회 시 가게가 존재하지 않으면 예외 발생")
    void getStoreDetail_storeNotFound() {
        // given
        Long storeId = 1L;

        Member member = Member.builder()
                .memberId(10L)
                .username("testUser")
                .build();

        when(memberRepository.findByUsername(member.getUsername())).thenReturn(Optional.of(member));

        when(storeRepository.findByStoreIdAndDeletedFalse(storeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetail(member.getUsername(), storeId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카테고리별 가게 목록 조회 성공")
    void getStoresByCategory_success() {
        // given
        String categoryCode = "burger";
        StoreCategory category = StoreCategory.builder()
                .categoryCode("burger")
                .categoryName("버거")
                .build();

        Store store1 = Store.builder().storeId(1L).storeName("버커킹 구름점").storeCategory(category).build();
        Store store2 = Store.builder().storeId(2L).storeName("맥도날드 구름점").storeCategory(category).build();

        StoreImage image1 = StoreImage.builder().store(store1).imageUrl("s3 url 1").build();
        StoreImage image2 = StoreImage.builder().store(store2).imageUrl("s3 url 2").build();

        when(storeCategoryRepository.findByCategoryCode(categoryCode)).thenReturn(Optional.of(category));
        when(storeRepository.findAllByStoreCategory_CategoryCodeAndDeletedFalse(categoryCode))
                .thenReturn(List.of(store1, store2));
        when(storeImageRepository.findImagesByStoreIds(List.of(1L, 2L)))
                .thenReturn(List.of(image1, image2));
        when(reviewRepository.findReviewStatsByStoreIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new Object[]{1L, 4.9, 1742L},
                        new Object[]{2L, 4.7, 2847L}
                ));

        // when
        StoreCategoryListResponse response = storeService.getStoresByCategory(categoryCode);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategory()).isEqualTo("burger");
        assertThat(response.getCategoryName()).isEqualTo("버거");
        assertThat(response.getStores()).hasSize(2);

        StoreDto dto1 = response.getStores().get(0);
        assertThat(dto1.getImageUrl()).isEqualTo("s3 url 1");
        assertThat(dto1.getName()).isEqualTo("버커킹 구름점");
        assertThat(dto1.getReview()).isEqualTo(4.9);
        assertThat(dto1.getReviewCount()).isEqualTo(1742);

        StoreDto dto2 = response.getStores().get(1);
        assertThat(dto2.getImageUrl()).isEqualTo("s3 url 2");
        assertThat(dto2.getName()).isEqualTo("맥도날드 구름점");
        assertThat(dto2.getReview()).isEqualTo(4.7);
        assertThat(dto2.getReviewCount()).isEqualTo(2847);
    }

    @Test
    @DisplayName("카테고리 코드가 없으면 예외 발생")
    void getStoresByCategory_categoryNotFound() {
        // given
        String categoryCode = "unknown";
        when(storeCategoryRepository.findByCategoryCode(categoryCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoresByCategory(categoryCode))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카테고리는 있지만 가게가 없으면 빈 리스트 반환")
    void getStoresByCategory_noStores() {
        String categoryCode = "burger";
        StoreCategory category = StoreCategory.builder()
                .categoryCode("burger")
                .categoryName("버거")
                .build();

        when(storeCategoryRepository.findByCategoryCode(categoryCode)).thenReturn(Optional.of(category));
        when(storeRepository.findAllByStoreCategory_CategoryCodeAndDeletedFalse(categoryCode))
                .thenReturn(Collections.emptyList());

        StoreCategoryListResponse response = storeService.getStoresByCategory(categoryCode);

        assertThat(response).isNotNull();
        assertThat(response.getCategory()).isEqualTo("burger");
        assertThat(response.getCategoryName()).isEqualTo("버거");
        assertThat(response.getStores()).isEmpty();
    }
}
