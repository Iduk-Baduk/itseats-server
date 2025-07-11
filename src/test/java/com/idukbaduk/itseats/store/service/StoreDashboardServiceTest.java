package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.dto.StoreDashboardResponse;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreDashboardServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock OrderRepository orderRepository;

    @InjectMocks
    OwnerStoreService ownerStoreService;

    @Test
    @DisplayName("가게 대시보드 정상 조회")
    void getDashboard_success() {
        // given
        Long storeId = 1L;
        Store store = Store.builder().storeId(storeId).storeName("테스트가게").build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(reviewRepository.findAverageRatingByStoreId(storeId)).thenReturn(4.5);
        when(orderRepository.findAverageCookTimeByStoreId(storeId)).thenReturn(25.0);
        when(orderRepository.countAccurateOrdersByStoreId(storeId)).thenReturn(8L);
        when(orderRepository.findAveragePickupTimeByStoreId(storeId)).thenReturn(5.0);
        when(orderRepository.countTotalOrdersByStoreId(storeId)).thenReturn(10L);
        when(orderRepository.countAcceptedOrdersByStoreId(storeId)).thenReturn(9L);

        // when
        StoreDashboardResponse response = ownerStoreService.getDashboard(storeId);

        // then
        assertThat(response.getStoreName()).isEqualTo("테스트가게");
        assertThat(response.getCustomerRating()).isEqualTo(4.5);
        assertThat(response.getAvgCookTime()).isEqualTo("25분");
        assertThat(response.getCookTimeAccuracy()).isEqualTo("80%");
        assertThat(response.getPickupTime()).isEqualTo("5분");
        assertThat(response.getOrderAcceptanceRate()).isEqualTo("90%");
    }

    @Test
    @DisplayName("가게가 없으면 예외 발생")
    void getDashboard_storeNotFound() {
        Long storeId = 999L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ownerStoreService.getDashboard(storeId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문/리뷰가 없을 때도 정상 동작")
    void getDashboard_emptyOrdersAndReviews() {
        Long storeId = 2L;
        Store store = Store.builder().storeId(storeId).storeName("빈가게").build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(reviewRepository.findAverageRatingByStoreId(storeId)).thenReturn(null);
        when(orderRepository.findAverageCookTimeByStoreId(storeId)).thenReturn(0.0);
        when(orderRepository.countAccurateOrdersByStoreId(storeId)).thenReturn(0L);
        when(orderRepository.findAveragePickupTimeByStoreId(storeId)).thenReturn(0.0);
        when(orderRepository.countTotalOrdersByStoreId(storeId)).thenReturn(0L);
        when(orderRepository.countAcceptedOrdersByStoreId(storeId)).thenReturn(0L);

        StoreDashboardResponse response = ownerStoreService.getDashboard(storeId);

        assertThat(response.getCustomerRating()).isEqualTo(0.0);
        assertThat(response.getAvgCookTime()).isEqualTo("0분");
        assertThat(response.getCookTimeAccuracy()).isEqualTo("0%");
        assertThat(response.getPickupTime()).isEqualTo("0분");
        assertThat(response.getOrderAcceptanceRate()).isEqualTo("0%");
    }
}
