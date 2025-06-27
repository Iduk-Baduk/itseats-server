package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.global.util.S3Utils;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Transactional
    public List<MenuImage> updateMenuImages(Menu menu, List<MultipartFile> images) {
        List<MenuImage> existingImages = menuImageRepository.findByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId());
        if (images == null) {
            // form-data에 images를 추가하지 않았다면 현재 상태 유지
            return existingImages;
        }

        List<String> existingImageUrls = existingImages.stream()
                .map(MenuImage::getImageUrl)
                .toList();

        // 기존 이미지 정보를 DB에서 삭제
        menuImageRepository.deleteAll(existingImages);

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
