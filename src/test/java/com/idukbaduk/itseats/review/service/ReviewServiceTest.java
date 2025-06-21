package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.review.dto.StoreReviewDto;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewImageRepository reviewImageRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("가게 별 리뷰 목록 조회 성공")
    @Test
    void getReviewsByStore_success() throws NoSuchFieldException, IllegalAccessException {
        // given
        Long storeId = 1L;
        Member member1 = Member.builder().nickname("홍길동").build();
        Member member2 = Member.builder().nickname("정재환").build();
        Store store = Store.builder().storeId(storeId).build();

        Review review1 = Review.builder()
                .reviewId(100L)
                .member(member1)
                .store(store)
                .storeStar(5)
                .content("정말 맛있어요!")
                .build();

        Field field = BaseEntity.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(review1, LocalDateTime.of(2025, 5, 5, 12, 34, 56));

        Review review2 = Review.builder()
                .reviewId(101L)
                .member(member2)
                .store(store)
                .storeStar(4)
                .content("또 먹고 싶어요!")
                .build();

        field.set(review2, LocalDateTime.of(2025, 5, 4, 12, 34, 56));

        ReviewImage image1 = ReviewImage.builder()
                .reviewImageId(1000L)
                .review(review1)
                .imageUrl("s3 url 1")
                .displayOrder(1)
                .build();

        ReviewImage image2 = ReviewImage.builder()
                .reviewImageId(1001L)
                .review(review2)
                .imageUrl("s3 url 2")
                .displayOrder(1)
                .build();

        when(reviewRepository.findReviewsWithMemberByStoreId(storeId))
                .thenReturn(List.of(review1, review2));
        when(reviewImageRepository.findByReviewIdInOrderByDisplayOrderAsc(List.of(100L, 101L)))
                .thenReturn(List.of(image1, image2));

        // when
        StoreReviewResponse response = reviewService.getReviewsByStore(storeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReviews()).hasSize(2);

        StoreReviewDto dto1 = response.getReviews().get(0);
        assertThat(dto1.getImageUrl()).isEqualTo("s3 url 1");
        assertThat(dto1.getReviewer()).isEqualTo("홍길동");
        assertThat(dto1.getRating()).isEqualTo(5);
        assertThat(dto1.getContent()).isEqualTo("정말 맛있어요!");
        assertThat(dto1.getCreatedAt()).isEqualTo("2025-05-05T12:34:56");

        StoreReviewDto dto2 = response.getReviews().get(1);
        assertThat(dto2.getImageUrl()).isEqualTo("s3 url 2");
        assertThat(dto2.getReviewer()).isEqualTo("정재환");
        assertThat(dto2.getRating()).isEqualTo(4);
        assertThat(dto2.getContent()).isEqualTo("또 먹고 싶어요!");
        assertThat(dto2.getCreatedAt()).isEqualTo("2025-05-04T12:34:56");
    }
}
