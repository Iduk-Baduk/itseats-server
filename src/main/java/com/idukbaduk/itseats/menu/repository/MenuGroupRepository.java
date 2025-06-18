package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuGroupRepository extends JpaRepository<MenuGroup, Long> {
    List<MenuGroup>
    findByStore_storeIdAndMenuGroupIsActiveTrueOrderByMenuGroupPriority(Long storeId);
}
