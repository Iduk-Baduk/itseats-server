package com.idukbaduk.itseats.store.repository;

import com.idukbaduk.itseats.store.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {
    @Query("SELECT si FROM StoreImage si " +
            "WHERE si.store.storeId IN :storeIds " +
            "ORDER BY si.store.storeId ASC, si.displayOrder ASC")
    List<StoreImage> findImagesByStoreIds(@Param("storeIds") List<Long> storeIds);

    @Query("SELECT si FROM StoreImage si " +
            "WHERE si.store.storeId = :storeId " +
            "ORDER BY si.displayOrder ASC")
    List<StoreImage> findAllByStoreIdOrderByDisplayOrderAsc(@Param("storeId") Long storeId);
}
