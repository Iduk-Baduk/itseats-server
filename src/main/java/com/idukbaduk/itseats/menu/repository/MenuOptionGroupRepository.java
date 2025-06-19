package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuOptionGroupRepository extends JpaRepository<MenuOptionGroup, Long> {

    List<MenuOptionGroup> findByMenu_MenuIdOrderByOptGroupPriority(Long menuMenuId);
}
