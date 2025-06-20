package com.idukbaduk.itseats.review.repository;

import com.idukbaduk.itseats.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.member WHERE r.store.storeId = :storeId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findReviewsWithMemberByStoreId(@Param("storeId") Long storeId);
}
