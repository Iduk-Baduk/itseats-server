package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.order.entity.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {

    @Query("""
        SELECT COUNT(om)
        FROM OrderMenu om
        WHERE om.order.orderId = :orderId
    """)
    Long countOrderMenus(@Param("orderId") Long orderId);
}
