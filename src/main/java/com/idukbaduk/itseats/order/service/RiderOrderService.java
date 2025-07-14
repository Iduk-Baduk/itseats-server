package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.AddressInfoDTO;
import com.idukbaduk.itseats.order.dto.RiderOrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.OrderItemDTO;
import com.idukbaduk.itseats.order.dto.OrderRequestResponse;
import com.idukbaduk.itseats.order.dto.RiderImageResponse;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.enums.AssignmentStatus;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import com.idukbaduk.itseats.rider.service.RiderService;
import com.idukbaduk.itseats.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RiderRepository riderRepository;

    private final RiderImageService riderImageService;
    private final RiderService riderService;

    @Transactional(readOnly = true)
    public RiderOrderDetailsResponse getOrderDetails(String username, Long orderId) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));
        Order order = orderRepository.findByRiderAndOrderId(rider, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return RiderOrderDetailsResponse.of(order, payment);
    }

    @Transactional
    public void acceptDelivery(String username, Long orderId) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateOrderStatusAccept(rider, OrderStatus.RIDER_READY);
        riderService.updateRiderAssignment(rider, order, AssignmentStatus.ACCEPTED);
    }

    @Transactional
    public void updateOrderStatus(String username, Long orderId, OrderStatus orderStatus) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));
        Order order = orderRepository.findByRiderAndOrderId(rider, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(orderStatus);

        if (orderStatus == OrderStatus.DELIVERED) {
            order.updateOrderEndTime(LocalDateTime.now());
        }
    }

    @Transactional
    public RiderImageResponse uploadRiderImage(String username, Long orderId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new OrderException(OrderErrorCode.REQUIRED_RIDER_IMAGE);
        }

        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));
        Order order = orderRepository.findByRiderAndOrderId(rider, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return buildRiderImageResponse(
                riderImageService.saveRiderImage(rider, order, image).getImageUrl()
        );
    }

    private RiderImageResponse buildRiderImageResponse(String imageUrl) {
        return RiderImageResponse.builder()
                .image(imageUrl)
                .build();
    }

    @Transactional
    public OrderRequestResponse getOrderRequest(String username) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));
        if (rider.getLocation() == null) {
            throw new RiderException(RiderErrorCode.RIDER_LOCATION_NOT_FOUND);
        }

        Order order = orderRepository
                .findCookedOrderByRiderLocation(rider.getLocation().getY(), rider.getLocation().getX())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        Store store = order.getStore();

        riderService.createRiderAssignment(rider, order);

        return buildOrderRequestResponse(rider, order, store);
    }

    private OrderRequestResponse buildOrderRequestResponse(Rider rider, Order order, Store store) {
        return OrderRequestResponse.builder()
                .orderId(order.getOrderId())
                .deliveryType(order.getDeliveryType().name())
                .storeName(store.getStoreName())
                .myLocation(buildAddressInfoDTO(rider.getLocation()))
                .storeLocation(buildAddressInfoDTO(store.getLocation()))
                .deliveryFee(order.getDeliveryFee())
                .storeAddress(store.getStoreAddress())
                .validTime(LocalDateTime.now().plusMinutes(1))
                .build();
    }

    private AddressInfoDTO buildAddressInfoDTO(Point location) {
        return AddressInfoDTO.builder()
                .lat(location.getY())
                .lng(location.getX())
                .build();
    }
}
