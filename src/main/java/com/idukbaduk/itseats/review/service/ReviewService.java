package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.review.dto.StoreReviewDto;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Transactional(readOnly = true)
    public StoreReviewResponse getReviewsByStore(Long storeId) {

        List<Review> reviews = reviewRepository.findReviewsWithMemberByStoreId(storeId);

        List<Long> reviewIds = reviews.stream()
                .map(Review::getReviewId)
                .toList();

        List<ReviewImage> images = reviewImageRepository
                .findByReviewIdInOrderByDisplayOrderAsc(reviewIds);

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
}
