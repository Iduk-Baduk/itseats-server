package com.idukbaduk.itseats.review.repository;

import com.idukbaduk.itseats.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findReviewIdByInOrderByDisplayOrderAsc(List<Long> reviewIds);
}

