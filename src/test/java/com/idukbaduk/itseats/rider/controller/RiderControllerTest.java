package com.idukbaduk.itseats.rider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.RejectDeliveryResponse;
import com.idukbaduk.itseats.rider.dto.RejectReasonRequest;
import com.idukbaduk.itseats.rider.dto.WorkingInfoResponse;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import com.idukbaduk.itseats.rider.service.RiderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiderController.class)
class RiderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiderService riderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("출/퇴근 상태 변경 성공")
    @WithMockUser(username = "testuser")
    void modifyWorking_success() throws Exception {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(true)
                .build();

        WorkingInfoResponse response = WorkingInfoResponse.builder()
                .isWorking(true)
                .build();

        when(riderService.modifyWorking(any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/rider/working")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.MODIFY_IS_WORKING_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(RiderResponse.MODIFY_IS_WORKING_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.isWorking").value(true));
    }

    @Test
    @DisplayName("출/퇴근 여부 누락시 예외 반환")
    @WithMockUser(username = "testuser")
    void modifyWorking_nullRequest() throws Exception {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder().build();

        // when & then
        mockMvc.perform(post("/api/rider/working")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("출/퇴근 여부는 필수값입니다."));
    }

    @Test
    @DisplayName("배달 거절 성공")
    @WithMockUser(username = "testuser")
    void rejectDelivery_success() throws Exception {
        // given
        long orderId = 1L;

        RejectReasonRequest request = RejectReasonRequest.builder()
                .rejectReason("사고 발생")
                .build();

        RejectDeliveryResponse response = RejectDeliveryResponse.builder()
                .rejectReason("사고 발생")
                .build();

        when(riderService.rejectDelivery(any(), any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.REJECT_DELIVERY_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.REJECT_DELIVERY_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("배달 거절 사유 누락시 예외 발생")
    @WithMockUser(username = "testuser")
    void rejectDelivery_nullReason() throws Exception {
        // given
        long orderId = 1L;
        RejectReasonRequest request = RejectReasonRequest.builder().build();

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("거절 사유는 필수값입니다."));
    }
}
