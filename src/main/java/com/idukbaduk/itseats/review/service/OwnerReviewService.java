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
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerReviewService {

    private final ReviewReportRepository reviewReportRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

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
}
