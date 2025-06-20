package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuOptionGroupRepository extends JpaRepository<MenuOptionGroup, Long> {

    @Query("SELECT DISTINCT g FROM MenuOptionGroup g " +
            "LEFT JOIN FETCH g.options o " +
            "WHERE g.menu.menuId = :menuId " +
            "ORDER BY g.optGroupPriority, o.optionPriority")
    List<MenuOptionGroup> findGroupsWithOptionsByMenuId(@Param("menuId") Long menuId);

}
