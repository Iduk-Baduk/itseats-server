package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuMediaService {
    // TODO S3에서 이미지 파일 저장 및 삭제

    private final MenuImageRepository menuImageRepository;

    public List<MenuImage> createMenuImages(Menu menu, List<MultipartFile> images) {
        if (images == null || images.isEmpty())
            return Collections.emptyList();

        // 이미지 파일 유효성 검증
        validateImageFile(images);

        return saveMenuImages(menu, images);
    }

    public List<MenuImage> updateMenuImages(Menu menu, List<MultipartFile> images) {
        List<MenuImage> existingImages = menuImageRepository.findByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId());

        if (images == null) {
            // form-data에 images를 추가하지 않았다면 현재 상태 유지
            return existingImages;
        }

        // 이미지 파일 유효성 검증
        validateImageFile(images);

        // 기존 이미지 삭제
        menuImageRepository.deleteAll(existingImages);

        // 빈 배열이면 빈 리스트 반환
        if (images.isEmpty()) {
            return Collections.emptyList();
        }

        // 새 이미지 저장
        return saveMenuImages(menu, images);
    }

    private void validateImageFile(List<MultipartFile> images) {
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new IllegalArgumentException("Empty image file is not allowed");
            }
        }
    }

    private List<MenuImage> saveMenuImages(Menu menu, List<MultipartFile> images) {
        List<MenuImage> menuImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = generateImageUrl(images.get(i));
            MenuImage menuImage = MenuImage.builder()
                    .menu(menu)
                    .imageUrl(imageUrl)
                    .displayOrder(i)
                    .build();
            menuImages.add(menuImage);
        }
        return menuImageRepository.saveAll(menuImages);
    }

    private String generateImageUrl(MultipartFile file) {
        // 임시 파일명 기반 URL
        return "https://example.com/" + file.getOriginalFilename();
    }
}
