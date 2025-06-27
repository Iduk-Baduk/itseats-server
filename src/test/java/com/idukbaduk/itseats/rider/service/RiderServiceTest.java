package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.Order;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;
    @Mock
    private RiderAssignmentRepository riderAssignmentRepository;

    @InjectMocks
    private RiderService riderService;

    private final String username = "testuser";
    private Member member;
    private Rider rider;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        rider = Rider.builder()
                .member(member)
                .riderId(1L)
                .build();
    }

    @Test
    @DisplayName("출근 상태 전환 성공")
    void modifyWorking_true_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(true)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("퇴근 상태 전환 성공")
    void modifyWorking_false_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(false)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("존재하지 않는 라이더 조회시 예외 발생")
    void modifyWorking_notExistRider() {
        // given
        when(riderRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderService.modifyWorking(username, ModifyWorkingRequest.builder().build()))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배달 거절 성공")
    void rejectDelivery_success() {
        // given
        RejectReasonRequest request = RejectReasonRequest.builder()
                .rejectReason("다른 배달 진행 예정입니다.")
                .build();

        RiderAssignment riderAssignment = RiderAssignment.builder()
                .assignmentId(1L)
                .rider(rider)
                .order(Order.builder()
                        .orderId(1L)
                        .build())
                .assignmentStatus(AssignmentStatus.PENDING)
                .build();

        when(riderAssignmentRepository.findByUsernameAndOrderId(username, 1L))
                .thenReturn(Optional.of(riderAssignment));

        // when
        RejectDeliveryResponse response = riderService.rejectDelivery(username, 1L, request);

        // then
        assertThat(response.getRejectReason()).isEqualTo(request.getRejectReason());
        assertThat(riderAssignment.getReason()).isEqualTo(request.getRejectReason());
        assertThat(riderAssignment.getAssignmentStatus()).isEqualTo(AssignmentStatus.REJECTED);
    }

    @Test
        @DisplayName("배차 관리 상태가 직전 단계가 아닌 경우 변경시 예외 발생")
    void rejectDelivery_rejectStatusFail() {
        // given
        RejectReasonRequest request = RejectReasonRequest.builder()
                .rejectReason("다른 배달 진행 예정입니다.")
                .build();

        RiderAssignment riderAssignment = RiderAssignment.builder()
                .assignmentId(1L)
                .rider(rider)
                .order(Order.builder()
                        .orderId(1L)
                        .build())
                .assignmentStatus(AssignmentStatus.ACCEPTED)
                .build();

        when(riderAssignmentRepository.findByUsernameAndOrderId(username, 1L))
                .thenReturn(Optional.of(riderAssignment));

        // when & then
        assertThatThrownBy(() -> riderService.rejectDelivery(username, 1L, RejectReasonRequest.builder().build()))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_ASSIGNMENT_STATUS_UPDATE_FAIL.getMessage());
    }

    @Test
    @DisplayName("라이더 주문 배차 테이블 조회 실패")
    void rejectDelivery_notExistRiderAssignment() {
        // given
        when(riderAssignmentRepository.findByUsernameAndOrderId(username, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderService.rejectDelivery(username, 1L, RejectReasonRequest.builder().build()))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_ASSIGNMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("라이더 배차 관리 정보가 성공적으로 저장")
    void createRiderAssignment_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .build();

        when(riderAssignmentRepository.save(any(RiderAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        riderService.createRiderAssignment(rider, order);

        // then
        ArgumentCaptor<RiderAssignment> captor = ArgumentCaptor.forClass(RiderAssignment.class);
        verify(riderAssignmentRepository).save(captor.capture());

        RiderAssignment savedRiderAssignment = captor.getValue();
        assertThat(savedRiderAssignment.getRider()).isEqualTo(rider);
        assertThat(savedRiderAssignment.getOrder()).isEqualTo(order);
        assertThat(savedRiderAssignment.getAssignmentStatus()).isEqualTo(AssignmentStatus.PENDING);
    }

    @Test
    @DisplayName("라이더 배차 상태 업데이트 성공")
    void updateRiderAssignment_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .rider(rider)
                .build();

        RiderAssignment assignment = RiderAssignment.builder()
                .assignmentId(1L)
                .rider(rider)
                .order(order)
                .assignmentStatus(AssignmentStatus.PENDING)
                .build();

        when(riderAssignmentRepository.findByRiderAndOrder(rider, order)).thenReturn(Optional.of(assignment));

        // when
        riderService.updateRiderAssignment(rider, order, AssignmentStatus.ACCEPTED);

        // then
        assertThat(assignment.getAssignmentStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
    }
}
