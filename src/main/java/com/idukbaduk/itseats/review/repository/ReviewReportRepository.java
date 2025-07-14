package com.idukbaduk.itseats.review.repository;

import com.idukbaduk.itseats.review.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByMember_MemberIdAndReview_ReviewId(Long memberId, Long reviewId);

    boolean existsByReview_ReviewId(Long reviewId);

}
