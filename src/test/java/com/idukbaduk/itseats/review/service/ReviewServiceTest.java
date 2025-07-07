package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.review.dto.ReviewCreateRequest;
import com.idukbaduk.itseats.review.dto.ReviewCreateResponse;
import com.idukbaduk.itseats.review.dto.StoreReviewDto;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ReviewService reviewService;

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
        when(reviewImageRepository.findByReviewReviewIdInOrderByDisplayOrderAsc(List.of(100L, 101L)))
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

    @Test
    @DisplayName("리뷰 작성 성공")
    void createReview_success() throws Exception {
        // given
        Long orderId = 1L;
        String username = "user1";
        Store store = Store.builder().storeId(10L).build();
        Rider rider = Rider.builder().riderId(20L).build();
        Order order = Order.builder()
                .orderId(orderId)
                .store(store)
                .rider(rider)
                .build();
        Member member = Member.builder().username(username).build();

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .storeStar(5)
                .riderStar(5)
                .menuLiked(MenuLiked.GOOD)
                .content("맛있어요")
                .build();

        Review savedReview = Review.builder()
                .reviewId(1L)
                .order(order)
                .store(store)
                .rider(rider)
                .member(member)
                .storeStar(request.getStoreStar())
                .riderStar(request.getRiderStar())
                .menuLiked(request.getMenuLiked())
                .content(request.getContent())
                .storeReply("")
                .build();

        // Redis 모킹
        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            return callback.doInRedis(null);
        });

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // when
        ReviewCreateResponse response = reviewService.createReview(orderId, username, request);

        // then
        assertThat(response.getStoreStar()).isEqualTo(5);
        assertThat(response.getRiderStar()).isEqualTo(5);
        assertThat(response.getMenuLiked()).isEqualTo(MenuLiked.GOOD);
        assertThat(response.getContent()).isEqualTo("맛있어요");

        verify(redisTemplate).execute(any(RedisCallback.class));

        // DB 저장 검증
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview.getStoreStar()).isEqualTo(5);
        assertThat(capturedReview.getRiderStar()).isEqualTo(5);
        assertThat(capturedReview.getMenuLiked()).isEqualTo(MenuLiked.GOOD);
        assertThat(capturedReview.getContent()).isEqualTo("맛있어요");
        assertThat(capturedReview.getStoreReply()).isEqualTo("");
    }
}
