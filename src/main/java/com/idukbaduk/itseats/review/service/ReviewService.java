package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.review.dto.ReviewCreateRequest;
import com.idukbaduk.itseats.review.dto.ReviewCreateResponse;
import com.idukbaduk.itseats.review.dto.StoreReviewDto;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    public StoreReviewResponse getReviewsByStore(Long storeId) {

        List<Review> reviews = reviewRepository.findReviewsWithMemberByStoreId(storeId);

        List<Long> reviewIds = reviews.stream()
                .map(Review::getReviewId)
                .toList();

        List<ReviewImage> images = reviewImageRepository
                .findByReviewReviewIdInOrderByDisplayOrderAsc(reviewIds);

        Map<Long, String> reviewIdToImageUrl = new HashMap<>();
        for (ReviewImage image : images) {
            Long reviewId = image.getReview().getReviewId();
            if (!reviewIdToImageUrl.containsKey(reviewId)) {
                reviewIdToImageUrl.put(reviewId, image.getImageUrl());
            }
        }

        List<StoreReviewDto> reviewDtos = reviews.stream()
                .map(review -> StoreReviewDto.builder()
                        .imageUrl(reviewIdToImageUrl.get(review.getReviewId()))
                        .reviewer(review.getMember().getNickname())
                        .rating(review.getStoreStar())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build())
                .toList();

        return StoreReviewResponse.builder()
                .reviews(reviewDtos)
                .build();
    }

    @Transactional
    public ReviewCreateResponse createReview(Long orderId, String username, ReviewCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Store store = order.getStore();
        Rider rider = order.getRider();
        Review review = Review.builder()
                .order(order)
                .member(member)
                .store(store)
                .rider(rider)
                .storeStar(request.getStoreStar())
                .riderStar(request.getRiderStar())
                .menuLiked(request.getMenuLiked())
                .content(request.getContent())
                .storeReply("")
                .build();

        reviewRepository.save(review);

        updateStoreStarCache(store.getStoreId(), request.getStoreStar());

        return ReviewCreateResponse.builder()
                .storeStar(review.getStoreStar())
                .riderStar(review.getRiderStar())
                .menuLiked(review.getMenuLiked())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private void updateStoreStarCache(Long storeId, int storeStar) {
        try {
            String key = "store:" + storeId + ":review:stats";
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.multi();
                byte[] keyBytes = key.getBytes();
                connection.hashCommands().hIncrBy(keyBytes, ("star_" + storeStar).getBytes(), 1L);
                connection.hashCommands().hIncrBy(keyBytes, "sum".getBytes(), (long) storeStar);
                connection.hashCommands().hIncrBy(keyBytes, "count".getBytes(), 1L);
                connection.exec();
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to update store review stats for storeId: {}", storeId, e);
            // 통계 업데이트 실패는 리뷰 생성을 막지 않음
        }
    }
}
