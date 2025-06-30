package com.idukbaduk.itseats.menu.repository;

import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.store.dto.MenuImageWithStoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface MenuImageRepository extends JpaRepository<MenuImage, Long> {
  
    Optional<MenuImage> findFirstByMenu_MenuIdOrderByDisplayOrderAsc(Long menuId);

    List<MenuImage> findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(List<Long> menuIds);

    List<MenuImage> findByMenu_MenuIdOrderByDisplayOrderAsc(Long menuId);

    @Query(value = "SELECT image_url AS imageUrl, store_id AS storeId " +
            "FROM (" +
            "  SELECT mi.image_url, s.store_id, " +
            "         ROW_NUMBER() OVER (PARTITION BY s.store_id ORDER BY mg.menu_group_priority, m.menu_priority) as rn " +
            "  FROM menu_image mi " +
            "  JOIN menu m ON mi.menu_id = m.menu_id " +
            "  JOIN menu_group mg ON m.menu_group_id = mg.menu_group_id " +
            "  JOIN store s ON mg.store_id = s.store_id " +
            "  WHERE s.store_id IN (:storeIds)" +
            ") ranked " +
            "WHERE ranked.rn <= 2", nativeQuery = true)
    List<MenuImageWithStoreId> findTop2ImagesPerStoreIds(List<Long> storeIds);
}
