package com.idukbaduk.itseats.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerStoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerStoreService ownerStoreService;

    @Test
    @DisplayName("가게 등록 성공 - multipart/form-data")
    @WithMockUser(username = "testuser")
    void createStore_success() throws Exception {
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
                any(StoreCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/owner/store-regist")
                        .param("name", "테스트가게")
                        .param("categoryName", "한식")
                        .param("isFranchise", "true")
                        .param("description", "설명")
                        .param("address", "서울시 강남구")
                        .param("locationX", "127.0")
                        .param("locationY", "37.5")
                        .param("phone", "010-1234-5678")
                        .param("defaultDeliveryFee", "3000")
                        .param("onlyOneDeliveryFee", "1000")
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
        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        StoreCreateResponse response = StoreCreateResponse.builder()
                .storeId(10L)
                .name("테스트가게")
                .build();

        when(ownerStoreService.createStore(anyString(), any(StoreCreateRequest.class)))
                .thenReturn(response);

        // when & then
        // when & then
        mockMvc.perform(multipart("/api/owner/store-regist")
                        .file(imageFile)
                        .param("name", "테스트가게")
                        .param("categoryName", "한식")
                        .param("isFranchise", "true")
                        .param("description", "설명")
                        .param("address", "서울시 강남구")
                        .param("locationX", "127.0")
                        .param("locationY", "37.5")
                        .param("phone", "010-1234-5678")
                        .param("defaultDeliveryFee", "3000")
                        .param("onlyOneDeliveryFee", "1000")
                )
                .andExpect(status().isCreated());

    }
}
