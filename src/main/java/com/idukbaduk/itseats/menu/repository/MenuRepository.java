package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuGroup mg " +
            "WHERE mg.store.storeId = :storeId " +
            "AND (:menuGroup IS NULL OR m.menuGroup.menuGroupName = :menuGroup) " +
            "AND (:keyword IS NULL OR m.menuName LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY mg.menuGroupPriority ASC, m.menuPriority ASC")
    List<Menu> findMenusByStore(
            @Param("storeId") Long storeId,
            @Param("menuGroup") String menuGroup,
            @Param("keyword") String keyword
    );

    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuGroup mg " +
            "WHERE m.menuId = :menuId " +
            "AND mg.store.storeId = :storeId")
    Optional<Menu> findByStoreIdAndMenuId(Long storeId, Long menuId);

    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuGroup mg " +
            "WHERE mg.store.storeId = :storeId")
    List<Menu> findByStoreId(Long storeId);
  
    Optional<Menu> findByMenuIdAndDeletedFalse(Long menuId);

    boolean existsByMenuGroup(MenuGroup menuGroup);

    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuGroup mg " +
            "LEFT JOIN FETCH m.menuOptionGroups mog " +
            "LEFT JOIN FETCH mog.options o " +
            "WHERE m.menuId = :menuId AND m.deleted = false ")
    Optional<Menu> findDetailById(@Param("menuId") Long menuId);
}
