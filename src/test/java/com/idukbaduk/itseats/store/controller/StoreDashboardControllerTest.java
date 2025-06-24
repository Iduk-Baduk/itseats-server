package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.store.dto.StoreDashboardResponse;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerStoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class StoreDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerStoreService ownerStoreService;

    @Test
    @DisplayName("가게 대시보드 조회 성공")
    void getStoreDashboard_success() throws Exception {
        StoreDashboardResponse response = StoreDashboardResponse.builder()
                .storeName("스타벅스")
                .customerRating(4.2)
                .avgCookTime("20분")
                .cookTimeAccuracy("98%")
                .pickupTime("43분")
                .orderAcceptanceRate("100%")
                .build();

        when(ownerStoreService.getDashboard(1L)).thenReturn(response);

        mockMvc.perform(get("/api/owner/1/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("가맹점 대시보드 조회 성공"))
                .andExpect(jsonPath("$.data.storeName").value("스타벅스"))
                .andExpect(jsonPath("$.data.customerRating").value(4.2))
                .andExpect(jsonPath("$.data.avgCookTime").value("20분"))
                .andExpect(jsonPath("$.data.cookTimeAccuracy").value("98%"))
                .andExpect(jsonPath("$.data.pickupTime").value("43분"))
                .andExpect(jsonPath("$.data.orderAcceptanceRate").value("100%"));
    }
}
