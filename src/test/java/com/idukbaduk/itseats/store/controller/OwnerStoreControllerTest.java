package com.idukbaduk.itseats.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderCookedResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.dto.StoreStatusUpdateRequest;
import com.idukbaduk.itseats.store.dto.StoreStatusUpdateResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerStoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerStoreService ownerStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("가게 등록 성공 - multipart/form-data")
    @WithMockUser(username = "testuser")
    void createStore_success() throws Exception {
        // given
        MockMultipartFile requestPart = getRequestMockMultipartFile();

        StoreCreateResponse response = StoreCreateResponse.builder()
                .storeId(10L)
                .name("테스트가게")
                .categoryName("한식")
                .isFranchise(true)
                .description("설명")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .build();

        when(ownerStoreService.createStore(
                anyString(),
                any(StoreCreateRequest.class),
                any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/owner/store-regist")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(StoreResponse.CREATE_STORE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.name").value("테스트가게"))
                .andExpect(jsonPath("$.data.categoryName").value("한식"))
                .andExpect(jsonPath("$.data.franchise").value(true));
    }

    @Test
    @DisplayName("이미지 파일과 함께 가게 등록 성공")
    @WithMockUser(username = "testuser")
    void createStore_withImages() throws Exception {
        // given
        MockMultipartFile requestPart = getRequestMockMultipartFile();
        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        StoreCreateResponse response = StoreCreateResponse.builder()
                .storeId(10L)
                .name("테스트가게")
                .build();

        when(ownerStoreService.createStore(anyString(), any(StoreCreateRequest.class), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/owner/store-regist")
                        .file(requestPart)
                        .file(imageFile)
                )
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("가게 상태 변경 성공 - 200 OK")
    @WithMockUser(username = "owner")
    void updateStatus_success() throws Exception {
        // given
        Long storeId = 1L;
        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(
                BusinessStatus.CLOSE,
                StoreStatus.REJECTED,
                true
        );

        // when & then
        mockMvc.perform(post("/api/owner/stores/{storeId}/status", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 상태 변경 실패 - 매장 없음")
    @WithMockUser(username = "owner")
    void updateStatus_storeNotFound() throws Exception {
        // given
        Long storeId = 99L;
        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest(null, null, null);

        doThrow(new StoreException(StoreErrorCode.STORE_NOT_FOUND))
                .when(ownerStoreService).updateStatus(eq(storeId), any(StoreStatusUpdateRequest.class));

        // when & then
        mockMvc.perform(post("/api/owner/stores/{storeId}/status", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    private MockMultipartFile getRequestMockMultipartFile() {
        String requestJson = """
        {
            "name": "테스트가게",
            "categoryName": "한식",
            "isFranchise": true,
            "description": "설명",
            "address": "서울시 강남구",
            "locationX": 127.0,
            "locationY": 37.5,
            "phone": "010-1234-5678",
            "defaultDeliveryFee": 3000,
            "onlyOneDeliveryFee": 1000
        }
        """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );
        return requestPart;
    }
}
