package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.order.entity.RiderImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiderImageRepository extends JpaRepository<RiderImage, Long> {
}
