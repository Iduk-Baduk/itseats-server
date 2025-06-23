package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.StoreDashboardResponse;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerStoreService {

    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public StoreDashboardResponse getDashboard(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        Double avgRating = reviewRepository.findAverageRatingByStoreId(storeId);
        double customerRating = avgRating != null ? avgRating : 0.0;

        Double avgCookTime = orderRepository.findAverageCookTimeByStoreId(storeId);
        Long accurateCount = orderRepository.countAccurateOrdersByStoreId(storeId);
        Double avgPickupTime = orderRepository.findAveragePickupTimeByStoreId(storeId);
        Long totalOrders = orderRepository.countTotalOrdersByStoreId(storeId);
        Long acceptedOrders = orderRepository.countAcceptedOrdersByStoreId(storeId);

        double cookTimeAccuracy = (totalOrders != null && totalOrders > 0 && accurateCount != null)
                ? accurateCount * 100.0 / totalOrders : 0.0;

        double acceptanceRate = (totalOrders != null && totalOrders > 0 && acceptedOrders != null)
                ? acceptedOrders * 100.0 / totalOrders : 0.0;

        return StoreDashboardResponse.builder()
                .storeName(store.getStoreName())
                .customerRating(customerRating)
                .avgCookTime((avgCookTime != null ? avgCookTime.intValue() : 0) + "분")
                .cookTimeAccuracy((int) cookTimeAccuracy + "%")
                .pickupTime((avgPickupTime != null ? avgPickupTime.intValue() : 0) + "분")
                .orderAcceptanceRate((int) acceptanceRate + "%")
                .build();
    }
}
