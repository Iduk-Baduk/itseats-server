package com.idukbaduk.itseats.review.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r.store.storeId, AVG(r.storeStar), COUNT(r) " +
            "FROM Review r WHERE r.store.storeId IN :storeIds GROUP BY r.store.storeId")
    List<Object[]> findReviewStatsByStoreIds(@Param("storeIds") List<Long> storeIds);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.member WHERE r.store.storeId = :storeId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findReviewsWithMemberByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT AVG(r.storeStar) FROM Review r WHERE r.store.storeId = :storeId")
    Double findAverageRatingByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.store.storeId = :storeId")
    int countByStoreId(@Param("storeId") Long storeId);

    List<Review> findByStore_StoreIdAndCreatedAtBetween(Long storeId, LocalDateTime start, LocalDateTime end);

    int countByMember(Member member);

    List<Review> findAllByMember(Member member);

    Optional<Review> findByReviewIdAndMember(Long reviewId, Member member);
}
