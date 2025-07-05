package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.order.dto.NearbyOrderDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.RejectDeliveryResponse;
import com.idukbaduk.itseats.rider.dto.RejectReasonRequest;
import com.idukbaduk.itseats.rider.dto.WorkingInfoResponse;
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

    private final RiderRepository riderRepository;
    private final RiderAssignmentRepository riderAssignmentRepository;
    private final OrderRepository orderRepository;


    @Transactional
    public WorkingInfoResponse modifyWorking(String username, ModifyWorkingRequest modifyWorkingRequest) {
        Rider rider = riderRepository.findByUsername(username)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        rider.modifyIsWorking(modifyWorkingRequest.getIsWorking());

        return WorkingInfoResponse.builder()
                .isWorking(rider.getIsWorking())
                .build();
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
    public List<NearbyOrderDTO> findNearbyOrders(double latitude, double longitude, int searchRadiusMeters) {

        return orderRepository.findNearbyOrders(longitude, latitude, searchRadiusMeters);
    }
}
