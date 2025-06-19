package com.idukbaduk.itseats.store.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("가맹점 정보를 성공적으로 반환")
    void getStore_success() {
        // given
        Long storeId = 1L;
        Store store = Store.builder()
                .storeId(storeId)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        Store result = storeService.getStore(storeId);

        // then
        assertThat(result.getStoreId()).isEqualTo(storeId);
    }

    @Test
    @DisplayName("존재하지 않는 가맹점 조회시 예외 발생")
    void getStore_notExist() {
        // given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStore(storeId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }
}
