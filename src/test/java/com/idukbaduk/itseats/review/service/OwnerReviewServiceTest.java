package com.idukbaduk.itseats.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.review.dto.ReviewDto;
import com.idukbaduk.itseats.review.dto.ReviewListResponse;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class OwnerReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewImageRepository reviewImageRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OwnerReviewService ownerReviewService;

    @Test
    @DisplayName("특정 기간 내 가게 별 리뷰 조회 성공")
    void getReviewsByStoreAndPeriod_success() throws NoSuchFieldException, IllegalAccessException {
        // given
        Long storeId = 1L;
        String username = "user1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        Member member = Member.builder().memberId(10L).username(username).nickname("user1").build();
        Store store = Store.builder().storeId(storeId).member(member).build();

        Review review = Review.builder()
                .reviewId(100L)
                .storeStar(5)
                .content("맛있어요")
                .member(member)
                .order(Order.builder()
                        .orderNumber("ORD123")
                        .orderMenus(List.of(
                                OrderMenu.builder().menuName("아이스 아메리카노")
                                        .build()))
                        .build())
                .build();

        Field createdAtField = review.getClass().getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(review, endDate);

        ReviewImage reviewImage = ReviewImage.builder()
                .reviewImageId(200L)
                .review(review)
                .imageUrl("http://image.url/1.jpg")
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(reviewRepository.findByStore_StoreIdAndCreatedAtBetween(storeId, startDate, endDate))
                .willReturn(List.of(review));
        given(reviewImageRepository.findByReview_ReviewIdIn(List.of(100L)))
                .willReturn(List.of(reviewImage));

        // when
        ReviewListResponse response = ownerReviewService.getReviewsByStoreAndPeriod(storeId, startDate, endDate, username);

        // then
        assertThat(response.getReviews()).hasSize(1);
        ReviewDto dto = response.getReviews().get(0);
        assertThat(dto.getMenuName()).isEqualTo("아이스 아메리카노");
        assertThat(dto.getOrderNumber()).isEqualTo("ORD123");
        assertThat(dto.getReviewImageUrl()).isEqualTo("http://image.url/1.jpg");
        assertThat(dto.getRating()).isEqualTo(5);
        assertThat(dto.getContent()).isEqualTo("맛있어요");
        assertThat(dto.getReviewer()).isEqualTo(username);
    }
}
