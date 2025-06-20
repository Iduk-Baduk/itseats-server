package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuGroupRepository extends JpaRepository<MenuGroup, Long> {

    @Query("SELECT DISTINCT g FROM MenuGroup g " +
            "LEFT JOIN FETCH g.menus m " +
            "WHERE g.store.storeId = :storeId " +
            "AND g.menuGroupIsActive = true " +
            "AND g.deleted = false " +
            "AND (m IS NULL OR m.deleted = false) " +
            "ORDER BY g.menuGroupPriority, m.menuPriority")
    List<MenuGroup> findGroupsWithMenusByStoreId(@Param("storeId") Long storeId);
}
