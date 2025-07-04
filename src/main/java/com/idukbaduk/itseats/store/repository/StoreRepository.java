package com.idukbaduk.itseats.store.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByMemberAndStoreId(Member member, Long storeId);

    List<Store> findAllByDeletedFalse();

    Optional<Store> findByStoreIdAndDeletedFalse(Long storeId);
  
    List<Store> findAllByStoreCategory_CategoryCodeAndDeletedFalse(String categoryCode);

    @Query(value = """
        SELECT * FROM store
        WHERE store_category_id = :storeCategoryId
        AND is_deleted = 0
        ORDER BY ST_Distance_Sphere(location, ST_GeomFromText(:myLocation, 4326))
    """, nativeQuery = true)
    Slice<Store> findNearByStoresByCategory(Long storeCategoryId, String myLocation, Pageable pageable);

    @Query(value = """
        SELECT s.* FROM store s
        LEFT JOIN review r ON r.store_id = s.store_id
        WHERE s.store_category_id = :storeCategoryId
          AND s.is_deleted = 0
        GROUP BY s.store_id
        ORDER BY AVG(IFNULL(r.store_star, 0)) DESC
    """, nativeQuery = true)
    Slice<Store> findStoresOrderByRating(Long storeCategoryId, Pageable pageable);

    @Query(value = """
        SELECT s.* FROM store s 
        LEFT JOIN orders o ON o.store_id = s.store_id
        WHERE s.store_category_id = :storeCategoryId
            AND s.is_deleted = 0
        GROUP BY s.store_id
        ORDER BY COUNT(o.order_id) DESC
    """, nativeQuery = true)
    Slice<Store> findStoresOrderByOrderCount(Long storeCategoryId, Pageable pageable);

    @Query(value = """
        SELECT * FROM store
        WHERE store_category_id = :storeCategoryId
            AND is_deleted = 0
        ORDER BY created_at DESC
    """, nativeQuery = true)
    Slice<Store> findStoresOrderByCreatedAt(Long storeCategoryId, Pageable pageable);
}
