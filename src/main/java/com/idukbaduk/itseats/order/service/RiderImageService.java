package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.global.util.S3Utils;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.RiderImage;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.RiderImageRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RiderImageService {

    private final RiderImageRepository riderImageRepository;
    private final S3Utils s3Utils;

    private static final String PATH = "rider_images/";

    public RiderImage saveRiderImage(Rider rider, Order order, MultipartFile image) {
        try {
            String imageUrl = s3Utils.uploadFileAndGetUrl(PATH, image);

            return riderImageRepository.save(
                    RiderImage.builder()
                            .imageUrl(imageUrl)
                            .rider(rider)
                            .order(order)
                            .build()
            );
        } catch (IOException | SdkException e) {
            throw new OrderException(OrderErrorCode.RIDER_IMAGE_IO_FAILED);
        }
    }
}
