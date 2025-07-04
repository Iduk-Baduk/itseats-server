package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.review.dto.StoreReviewStats;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReviewRepository reviewRepository;

    public StoreReviewStats getReviewStats(Long storeId) {
        String key = "store:" + storeId + ":review:stats";
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        try {
            Object sumObj = ops.get(key, "sum");
            Object countObj = ops.get(key, "count");
            double avg = 0.0;
            int count = 0;
            if (sumObj != null && countObj != null) {
                int sum = Integer.parseInt(sumObj.toString());
                count = Integer.parseInt(countObj.toString());
                avg = count > 0 ? roundToOneDecimal((double) sum / count) : 0.0;
            }
            return new StoreReviewStats(avg, count);
        } catch (Exception e) {
            log.warn("Redis에서 리뷰 통계 조회 실패, DB 폴백 실행. storeId: {}, error: {}", storeId, e.getMessage());
            // Redis 장애 시 DB에서 조회
            Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
            int reviewCount = reviewRepository.countByStoreId(storeId);
            return new StoreReviewStats(avgRating != null ? roundToOneDecimal(avgRating) : 0.0, reviewCount);
        }
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
