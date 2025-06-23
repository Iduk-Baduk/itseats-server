package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreMediaService {

    private final StoreImageRepository storeImageRepository;

    public void createStoreImages(Store store, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;

        int order = 0;
        for (MultipartFile image : images) {
            // 임시 Url 생성
            String imageUrl = generateImageUrl(image);

            StoreImage storeImage = StoreImage.builder()
                    .store(store)
                    .imageUrl(imageUrl)
                    .displayOrder(order++)
                    .build();
            storeImageRepository.save(storeImage);
        }
    }

    private String generateImageUrl(MultipartFile file) {
        // 임시 파일명 기반 URL
        return "https:/example/" + file.getOriginalFilename();
    }
}
