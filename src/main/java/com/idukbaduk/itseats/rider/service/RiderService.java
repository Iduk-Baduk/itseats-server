package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.dto.NearbyOrderDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.rider.dto.*;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.RiderAssignment;
import com.idukbaduk.itseats.rider.entity.enums.AssignmentStatus;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderAssignmentRepository;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final MemberRepository memberRepository;
    private final RiderRepository riderRepository;
    private final RiderAssignmentRepository riderAssignmentRepository;
    private final OrderRepository orderRepository;

    private static final int DEFAULT_SEARCH_RADIUS_KM = 10;

    @Transactional
    public void createRider(String username, RiderInfoRequest request) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        riderRepository.save(request.toRider(member));
    }

    @Transactional
    public WorkingInfoResponse modifyWorking(String username, ModifyWorkingRequest modifyWorkingRequest) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        rider.modifyIsWorking(modifyWorkingRequest.getIsWorking());

        return WorkingInfoResponse.builder()
                .isWorking(rider.getIsWorking())
                .build();
    }

    @Transactional
    public void updateLocation(String username, LocationRequest request) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        Rider rider = riderRepository.findByMember(member).orElseThrow(
                () -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND)
        );
        rider.updateLocation(GeoUtil.toPoint(request.getLongitude(), request.getLatitude()));
    }

    public void createRiderAssignment(Rider rider, Order order) {
        riderAssignmentRepository.save(buildRiderAssignment(rider, order));
    }

    private RiderAssignment buildRiderAssignment(Rider rider, Order order) {
        return RiderAssignment.builder()
                .rider(rider)
                .order(order)
                .assignmentStatus(AssignmentStatus.PENDING)
                .build();
    }

    @Transactional
    public RejectDeliveryResponse rejectDelivery(String username, Long orderId, RejectReasonRequest reasonRequest) {
        RiderAssignment riderAssignment = riderAssignmentRepository.findByUsernameAndOrderId(username, orderId)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_ASSIGNMENT_NOT_FOUND));

        riderAssignment.rejectDelivery(AssignmentStatus.REJECTED, reasonRequest.getRejectReason());

        return buildRejectDeliveryResponse(riderAssignment.getReason());
    }

    private RejectDeliveryResponse buildRejectDeliveryResponse(String reason) {
        return RejectDeliveryResponse.builder()
                .rejectReason(reason)
                .build();
    }

    public void updateRiderAssignment(Rider rider, Order order, AssignmentStatus assignmentStatus) {
        RiderAssignment riderAssignment = riderAssignmentRepository.findByRiderAndOrder(rider, order)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_ASSIGNMENT_NOT_FOUND));

        riderAssignment.updateAssignmentStatus(assignmentStatus);
    }

    @Transactional(readOnly = true)
    public List<ReadyOrderResponse> findNearbyOrders(LocationRequest request) {
        final int searchRadiusMeters = DEFAULT_SEARCH_RADIUS_KM * 1000; // 10km

        List<NearbyOrderDTO> nearbyOrders = orderRepository.findNearbyOrders(
                request.getLongitude(),
                request.getLatitude(),
                searchRadiusMeters
        );

        if (nearbyOrders.isEmpty()) {
            throw new RiderException(RiderErrorCode.NEARBY_ORDERS_NOT_FOUND);
        }

        return nearbyOrders.stream()
                .map(dto -> ReadyOrderResponse.builder()
                        .deliveryType(dto.getDeliveryType())
                        .storeName(dto.getStoreName())
                        .deliveryDistance(Math.round(dto.getDistance() / 100.0) / 10.0)
                        .deliveryFee(dto.getDeliveryFee())
                        .deliveryAddress(dto.getDeliveryAddress())
                        .build())
                .toList();
    }
}
