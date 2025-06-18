package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu>
    findByMenuGroup_MenuGroupIdAndDeletedFalseOrderByMenuPriority(Long memberGroupId);
}
