package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT MIN(FUNCTION('TIMESTAMPDIFF', 'MINUTE', o.createdAt, o.modifiedAt))
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.deliveryType = :deliveryType
    """)
    Integer findMinDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    @Query("""
        SELECT MAX(FUNCTION('TIMESTAMPDIFF', 'MINUTE', o.createdAt, o.modifiedAt))
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.deliveryType = :deliveryType
    """)
    Integer findMaxDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    @Query("""
        SELECT AVG(FUNCTION('TIMESTAMPDIFF', 'SECOND', o.createdAt, o.modifiedAt))
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.deliveryType = :deliveryType
    """)
    Long findAvgDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    @Query("""
        SELECT o.orderNumber
        FROM Order o
        WHERE o.orderNumber = :orderNumber
    """)
    boolean existsOrderNumber(@Param("orderNumber") String orderNumber);
}
