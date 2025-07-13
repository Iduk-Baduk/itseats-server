package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.review.dto.ReviewReportRequest;
import com.idukbaduk.itseats.review.dto.ReviewReportResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewReport;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.review.repository.ReviewReportRepository;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.review.dto.ReviewDto;
import com.idukbaduk.itseats.review.dto.ReviewListResponse;
import com.idukbaduk.itseats.review.entity.ReviewImage;
import com.idukbaduk.itseats.review.repository.ReviewImageRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerReviewService {

    private final ReviewReportRepository reviewReportRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByStoreAndPeriod(
            Long storeId, LocalDateTime startDate, LocalDateTime endDate, String username
    ) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        if (!store.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StoreException(StoreErrorCode.NOT_STORE_OWNER);
        }
        List<Review> reviews = fetchReviews(storeId, startDate, endDate);
        List<Long> reviewIds = reviews.stream().map(Review::getReviewId).collect(Collectors.toList());
        Map<Long, String> reviewIdToImageUrl = fetchReviewImages(reviewIds);
        List<ReviewDto> reviewDtos = convertToDto(reviews, reviewIdToImageUrl);

        return ReviewListResponse.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .reviews(reviewDtos)
                .build();
    }
  
    @Transactional
    public ReviewReportResponse reportReview(
            String username, Long storeId, Long reviewId, ReviewReportRequest request
    ) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        if (!store.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StoreException(StoreErrorCode.NOT_STORE_OWNER);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (reviewReportRepository.existsByMember_MemberIdAndReview_ReviewId(member.getMemberId(), review.getReviewId())) {
            throw new ReviewException(ReviewErrorCode.ALREADY_REPORTED);
        }

        ReviewReport report = ReviewReport.create(member, review, request.getReason());

        reviewReportRepository.save(report);

        return ReviewReportResponse.builder()
                .reporter(member.getUsername())
                .reviewId(review.getReviewId())
                .reason(report.getReason())
                .reportedAt(report.getCreatedAt())
                .reportStatus(report.getReportStatus())
                .build();
    }

    private List<Review> fetchReviews(Long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.findByStore_StoreIdAndCreatedAtBetween(storeId, startDate, endDate);
    }

    private Map<Long, String> fetchReviewImages(List<Long> reviewIds) {
        List<ReviewImage> images = reviewImageRepository.findByReview_ReviewIdIn(reviewIds);
        return images.stream()
                .collect(Collectors.toMap(
                        img -> img.getReview().getReviewId(),
                        ReviewImage::getImageUrl
                ));
    }

    private List<ReviewDto> convertToDto(List<Review> reviews, Map<Long, String> reviewIdToImageUrl) {
        return reviews.stream()
                .map(review -> {
                    String menuName = null;
                    String orderNumber = null;
                    if (review.getOrder() != null) {
                        List<OrderMenu> orderMenus = review.getOrder().getOrderMenus();
                        if (orderMenus != null && !orderMenus.isEmpty()) {
                            menuName = orderMenus.get(0).getMenuName();
                        }
                        orderNumber = review.getOrder().getOrderNumber();
                    }
                    String reviewer = review.getMember().getNickname();
                    return ReviewDto.builder()
                            .reviewId(review.getReviewId())
                            .reviewer(reviewer)
                            .menuName(menuName)
                            .orderNumber(orderNumber)
                            .reviewImageUrl(reviewIdToImageUrl.get(review.getReviewId()))
                            .rating(review.getStoreStar())
                            .content(review.getContent())
                            .createdAt(review.getCreatedAt())
                            .build();
                })
                .toList();
    }
}
