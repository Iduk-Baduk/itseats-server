package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.NearbyOrderDTO;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public List<NearbyOrderDTO> findNearbyOrders(double latitude, double longitude, int searchRadiusMeters) {

        return orderRepository.findNearbyOrders(longitude, latitude, searchRadiusMeters);
    }
}
