package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuImageRepository extends JpaRepository<MenuImage, Long> {
    List<MenuImage> findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(List<Long> menuIds);
}
