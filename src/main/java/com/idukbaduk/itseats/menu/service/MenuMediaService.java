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

    private final MenuImageRepository menuImageRepository;

    public List<MenuImage> createMenuImages(Menu menu, List<MultipartFile> images) {
        if (images == null || images.isEmpty())
            return Collections.emptyList();

        // 이미지 파일 유효성 검증
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new IllegalArgumentException("Empty image file is not allowed");
            }
        }

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
