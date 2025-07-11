package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.global.util.S3Utils;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.RiderImage;
import com.idukbaduk.itseats.order.repository.RiderImageRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderImageServiceTest {

    @Mock
    private RiderImageRepository riderImageRepository;

    @Mock
    private S3Utils s3Utils;

    @InjectMocks
    private RiderImageService riderImageService;

    private Rider rider;
    private Order order;
    private MultipartFile multipartFile;
    private RiderImage riderImage;

    @BeforeEach
    void setUp() {
        rider = Rider.builder()
                .riderId(1L)
                .build();

        order = Order.builder()
                .orderId(1L)
                .build();

        multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test".getBytes()
        );

        riderImage = RiderImage.builder()
                .imageId(1L)
                .rider(rider)
                .order(order)
                .imageUrl("https://example.com/test.jpg")
                .build();
    }

    @Test
    @DisplayName("배달 상태 사진 저장 성공")
    void saveRiderImage_success() throws IOException {
        // given
        when(riderImageRepository.save(any(RiderImage.class))).thenReturn(riderImage);
        ArgumentCaptor<RiderImage> captor = ArgumentCaptor.forClass(RiderImage.class);

        when(s3Utils.uploadFileAndGetUrl(anyString(), any())).thenReturn(multipartFile.getOriginalFilename());

        // when
        RiderImage savedRiderImage = riderImageService.saveRiderImage(rider, order, multipartFile);

        // then
        verify(s3Utils).uploadFileAndGetUrl(anyString(), any());
        verify(riderImageRepository).save(captor.capture());
        RiderImage capturedImage = captor.getValue();
        assertThat(capturedImage.getRider()).isEqualTo(rider);
        assertThat(capturedImage.getOrder()).isEqualTo(order);
        assertThat(capturedImage.getImageUrl()).contains(multipartFile.getOriginalFilename());
        assertThat(savedRiderImage.getImageId()).isEqualTo(riderImage.getImageId());
        assertThat(savedRiderImage.getRider()).isEqualTo(rider);
        assertThat(savedRiderImage.getOrder()).isEqualTo(order);
        assertThat(savedRiderImage.getImageUrl()).isEqualTo(riderImage.getImageUrl());
    }
}
