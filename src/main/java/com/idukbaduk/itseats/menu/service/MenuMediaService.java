package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.global.S3Config;
import com.idukbaduk.itseats.global.util.S3Utils;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuMediaService {

    private final MenuImageRepository menuImageRepository;
    private final S3Utils s3Utils;

    private static final String PATH = "menu_images/";

    public List<MenuImage> createMenuImages(Menu menu, List<MultipartFile> images) {
        if (images == null || images.isEmpty())
            return Collections.emptyList();

        // 이미지 파일 유효성 검증
        images = filterValidImages(images);

        try {
            return saveMenuImages(menu, images);
        } catch (IOException | SdkException | NullPointerException e) {
            throw new MenuException(MenuErrorCode.MENU_IMAGE_IO_FAILED);
        }
    }

    public List<MenuImage> updateMenuImages(Menu menu, List<MultipartFile> images) {
        List<MenuImage> existingImages = menuImageRepository.findByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId());
        if (images == null) {
            // form-data에 images를 추가하지 않았다면 현재 상태 유지
            return existingImages;
        }

        // 기존 이미지 삭제
        existingImages.forEach(image -> {
            s3Utils.deleteFile(image.getImageUrl());
        });
        menuImageRepository.deleteAll(existingImages);

        // 이미지 파일 유효성 검증
        images = filterValidImages(images);

        // 빈 배열이면 빈 리스트 반환
        if (images.isEmpty()) {
            return Collections.emptyList();
        }

        // 새 이미지 저장
        try {
            return saveMenuImages(menu, images);
        } catch (Exception e) {
            throw new MenuException(MenuErrorCode.MENU_IMAGE_IO_FAILED);
        }
    }

    private List<MultipartFile> filterValidImages(List<MultipartFile> images) {
        if (images == null) return Collections.emptyList();

        return images.stream()
                .filter(image -> !image.isEmpty())
                .toList();
    }

    private List<MenuImage> saveMenuImages(Menu menu, List<MultipartFile> images) throws IOException {
        List<MenuImage> menuImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = s3Utils.uploadFileAndGetUrl(PATH, images.get(i));
            MenuImage menuImage = MenuImage.builder()
                    .menu(menu)
                    .imageUrl(imageUrl)
                    .displayOrder(i)
                    .build();
            menuImages.add(menuImage);
        }
        return menuImageRepository.saveAll(menuImages);
    }
}
