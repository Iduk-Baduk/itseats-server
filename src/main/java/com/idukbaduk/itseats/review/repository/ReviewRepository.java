package com.idukbaduk.itseats.review.repository;

import com.idukbaduk.itseats.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r.store.storeId, AVG(r.storeStar), COUNT(r) " +
            "FROM Review r WHERE r.store.storeId IN :storeIds GROUP BY r.store.storeId")
    List<Object[]> findReviewStatsByStoreIds(@Param("storeIds") List<Long> storeIds);
}
