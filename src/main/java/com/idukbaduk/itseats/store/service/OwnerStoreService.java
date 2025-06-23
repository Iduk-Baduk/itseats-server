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

        List<Order> orders = orderRepository.findAllByStore_StoreId(storeId);

        double avgCookTime = calculateAverageCookTime(orders);
        double cookTimeAccuracy = calculateCookTimeAccuracy(orders);
        double avgPickupTime = calculateAveragePickupTime(orders);
        double acceptanceRate = calculateOrderAcceptanceRate(orders);

        return StoreDashboardResponse.builder()
                .storeName(store.getStoreName())
                .customerRating(customerRating)
                .avgCookTime((int) avgCookTime + "분")
                .cookTimeAccuracy((int) cookTimeAccuracy + "%")
                .pickupTime((int) avgPickupTime + "분")
                .orderAcceptanceRate((int) acceptanceRate + "%")
                .build();
    }

    private double calculateAverageCookTime(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getCookStartTime() != null && o.getOrderEndTime() != null)
                .mapToLong(o -> Duration.between(o.getCookStartTime(), o.getOrderEndTime()).toMinutes())
                .average()
                .orElse(0.0);
    }

    private double calculateCookTimeAccuracy(List<Order> orders) {
        long total = orders.stream()
                .filter(o -> o.getOrderEndTime() != null && o.getDeliveryEta() != null)
                .count();

        long accurate = orders.stream()
                .filter(o -> o.getOrderEndTime() != null && o.getDeliveryEta() != null)
                .filter(o -> {
                    long diff = Math.abs(Duration.between(o.getDeliveryEta(), o.getOrderEndTime()).toMinutes());
                    return diff <= 5;
                })
                .count();

        return total == 0 ? 0.0 : accurate * 100.0 / total;
    }

    private double calculateAveragePickupTime(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getOrderReceivedTime() != null && o.getOrderEndTime() != null)
                .mapToLong(o -> Duration.between(o.getOrderReceivedTime(), o.getOrderEndTime()).toMinutes())
                .average()
                .orElse(0.0);
    }

    private double calculateOrderAcceptanceRate(List<Order> orders) {
        long total = orders.size();
        long accepted = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COOKING ||
                        o.getOrderStatus() == OrderStatus.RIDER_READY ||
                        o.getOrderStatus() == OrderStatus.DELIVERING ||
                        o.getOrderStatus() == OrderStatus.DELIVERED ||
                        o.getOrderStatus() == OrderStatus.COMPLETED)
                .count();
        return total == 0 ? 0.0 : accepted * 100.0 / total;
    }
}
