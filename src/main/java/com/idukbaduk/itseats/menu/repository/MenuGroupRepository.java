package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuGroupRepository extends JpaRepository<MenuGroup, Long> {

    @Query("SELECT DISTINCT g FROM MenuGroup g " +
            "LEFT JOIN FETCH g.menus m " +
            "WHERE g.store.storeId = :storeId " +
            "AND g.menuGroupIsActive = true " +
            "AND g.deleted = false " +
            "AND (m IS NULL OR m.deleted = false) " +
            "ORDER BY g.menuGroupPriority, m.menuPriority")
    List<MenuGroup> findGroupsWithMenusByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT g FROM MenuGroup g " +
            "WHERE g.store.storeId = :storeId " +
            "ORDER BY g.menuGroupPriority")
    List<MenuGroup> findMenuGroupsByStoreId(@Param("storeId") Long storeId);

    Optional<MenuGroup> findMenuGroupByMenuGroupNameAndStore_StoreId(String menuGroupName, Long storeId);
}
