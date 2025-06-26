package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.RiderImage;
import com.idukbaduk.itseats.order.repository.RiderImageRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RiderImageService {

    private final RiderImageRepository riderImageRepository;

    public RiderImage saveRiderImage(Rider rider, Order order, MultipartFile image) {
        return riderImageRepository.save(
                RiderImage.builder()
                        .imageUrl(generateImageUrl(image))
                        .rider(rider)
                        .order(order)
                        .build()
        );
    }

    private String generateImageUrl(MultipartFile image) {
        // TODO: S3 연동 구현 필요
        return "https://example.com/" + image.getOriginalFilename();
    }
}
