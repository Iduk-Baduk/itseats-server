package com.idukbaduk.itseats.store.repository;

import com.idukbaduk.itseats.store.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {
    Optional<StoreCategory> findByCategoryCode(String categoryCode);
}
