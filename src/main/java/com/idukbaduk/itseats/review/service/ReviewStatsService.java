package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.review.dto.StoreReviewStats;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReviewRepository reviewRepository;

    private static final String REVIEW_STATS_KEY_PREFIX = "store:";
    private static final String REVIEW_STATS_KEY_SUFFIX = ":review:stats";

    public StoreReviewStats getReviewStats(Long storeId) {
        String key = REVIEW_STATS_KEY_PREFIX + storeId + REVIEW_STATS_KEY_SUFFIX;
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
            Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
            int reviewCount = reviewRepository.countByStoreId(storeId);
            return new StoreReviewStats(avgRating != null ? roundToOneDecimal(avgRating) : 0.0, reviewCount);
        }
    }

    public Map<Long, StoreReviewStats> getReviewStatsForStores(List<Long> storeIds) {
        Map<Long, StoreReviewStats> resultMap = new HashMap<>();
        if (storeIds.isEmpty()) {
            return resultMap;
        }

        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        List<String> keys = storeIds.stream()
                .map(id -> REVIEW_STATS_KEY_PREFIX + id + REVIEW_STATS_KEY_SUFFIX)
                .toList();

        try {
            // Redis에서 한 번에 여러 키의 데이터를 조회
            Map<String, Map<Object, Object>> multipleHash = new HashMap<>();
            keys.forEach(key -> {
                Map<Object, Object> entries = ops.entries(key);
                if (!entries.isEmpty()) {
                    multipleHash.put(key, entries);
                }
            });
            for (Long storeId : storeIds) {
                String key = REVIEW_STATS_KEY_PREFIX + storeId + REVIEW_STATS_KEY_SUFFIX;
                Map<Object, Object> hashEntries = multipleHash.get(key);

                if (hashEntries != null && !hashEntries.isEmpty()) {
                    Object sumObj = hashEntries.get("sum");
                    Object countObj = hashEntries.get("count");
                    if (sumObj != null && countObj != null) {
                        int sum = Integer.parseInt(sumObj.toString());
                        int count = Integer.parseInt(countObj.toString());
                        double avg = count > 0 ? roundToOneDecimal((double) sum / count) : 0.0;
                        resultMap.put(storeId, new StoreReviewStats(avg, count));
                    }
                }
            }

            List<Long> missingStoreIds = storeIds.stream()
                    .filter(id -> !resultMap.containsKey(id))
                    .toList();

            if (!missingStoreIds.isEmpty()) {
                Map<Long, StoreReviewStats> dbStats = getReviewStatsFromDB(missingStoreIds);
                resultMap.putAll(dbStats);
            }
        } catch (Exception e) {
            log.warn("Redis에서 리뷰 통계 일괄 조회 실패, DB 폴백 실행. error: {}", e.getMessage());
            return getReviewStatsFromDB(storeIds);
        }
        return resultMap;
    }

    private Map<Long, StoreReviewStats> getReviewStatsFromDB(List<Long> storeIds) {
        Map<Long, StoreReviewStats> resultMap = new HashMap<>();
        List<Object[]> stats = reviewRepository.findReviewStatsByStoreIds(storeIds);

        for (Object[] row : stats) {
            Long storeId = (Long) row[0];
            double avg = row[1] != null ? roundToOneDecimal((Double) row[1]) : 0.0;
            Long count = (Long) row[2];
            resultMap.put(storeId, new StoreReviewStats(avg, count.intValue()));
        }

        storeIds.forEach(id -> resultMap.putIfAbsent(id, new StoreReviewStats(0.0, 0)));

        return resultMap;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
