package com.idukbaduk.itseats.store.service;

import com.idukbaduk.itseats.global.util.S3Utils;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreMediaService {

    private final StoreImageRepository storeImageRepository;
    private final S3Utils s3Utils;

    private static final String PATH = "store_images/";

    public List<StoreImage> createStoreImages(Store store, List<MultipartFile> images) {
        if (images == null || images.isEmpty())
            return Collections.emptyList();

        // 이미지 파일 유효성 검증
        images = filterValidImages(images);

        try {
            return saveStoreImages(store, images);
        } catch (IOException | SdkException | NullPointerException e) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_IO_FAILED);
        }
    }
    private List<MultipartFile> filterValidImages(List<MultipartFile> images) {
        if (images == null) return Collections.emptyList();

        return images.stream()
                .filter(image -> !image.isEmpty())
                .toList();
    }

    @Transactional
    public List<StoreImage> updateStoreImages(Store store, List<MultipartFile> images) {
        List<StoreImage> existingImages = storeImageRepository.findAllByStoreIdOrderByDisplayOrderAsc(store.getStoreId());
        if (images == null) {
            // form-data에 images를 추가하지 않았다면 현재 상태 유지
            return existingImages;
        }

        List<String> existingImageUrls = existingImages.stream()
                .map(StoreImage::getImageUrl)
                .toList();

        // 기존 이미지 정로를 DB에서 삭제
        storeImageRepository.deleteAll(existingImages);

        try {
            existingImageUrls.forEach(s3Utils::deleteFile);
        } catch (Exception e) {
            // S3 삭제 실패는 로그만 남기고 진행 (DB에서 이미 전체 삭제됨)
            log.error("S3 파일 삭제 실패", e);
        }

        // 이미지 파일 유효성 검증
        images = filterValidImages(images);

        // 빈 배열이면 빈 리스트 반환
        if (images.isEmpty()) {
            return Collections.emptyList();
        }

        // 새 이미지 저장
        try {
            return saveStoreImages(store, images);
        } catch (Exception e) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_IO_FAILED);
        }
    }

    private List<StoreImage> saveStoreImages(Store store, List<MultipartFile> images) throws IOException {
        List<StoreImage> storeImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {

            String imageUrl = "text/plain".equalsIgnoreCase(images.get(i).getContentType())
                    ? new String(images.get(i).getBytes(), StandardCharsets.UTF_8).trim() // 이미지 링크 업로드
                    : s3Utils.uploadFileAndGetUrl(PATH, images.get(i)); // 이미지 파일 업로드

            StoreImage storeImage = StoreImage.builder()
                    .store(store)
                    .imageUrl(imageUrl)
                    .displayOrder(i)
                    .build();
            storeImages.add(storeImage);

        }
        return storeImageRepository.saveAll(storeImages);
    }
}
