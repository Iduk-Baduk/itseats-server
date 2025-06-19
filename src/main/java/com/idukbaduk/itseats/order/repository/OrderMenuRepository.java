package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.order.entity.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {
}
