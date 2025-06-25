package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuMediaService {

    private final MenuImageRepository menuImageRepository;

    public List<MenuImage> createMenuImages(Menu menu, List<MultipartFile> images) {
        if (images == null || images.isEmpty())
            return Collections.emptyList();

        int order = 0;
        for (MultipartFile image : images) {
            // 임시 URL 생성
            String imageUrl = generateImageUrl(image);

            MenuImage menuImage = MenuImage.builder()
                    .menu(menu)
                    .imageUrl(imageUrl)
                    .displayOrder(order++)
                    .build();
            menuImageRepository.save(menuImage);
        }
        return menuImageRepository.findByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId());
    }

    private String generateImageUrl(MultipartFile file) {
        // 임시 파일명 기반 URL
        return "https://example.com/" + file.getOriginalFilename();
    }
}
