package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.review.dto.StoreReviewStats;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
                avg = count > 0 ? Math.round((sum * 10.0 / count)) / 10.0 : 0.0;
            }
            return new StoreReviewStats(avg, count);
        } catch (Exception e) {
            // Redis 장애 시 DB에서 조회
            Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
            int reviewCount = reviewRepository.countByStoreId(storeId);
            return new StoreReviewStats(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0, reviewCount);
        }
    }
}
