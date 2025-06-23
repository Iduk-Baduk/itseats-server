package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        SELECT MIN(TIMESTAMPDIFF(MINUTE, created_at, modified_at))
        FROM orders
        WHERE order_status = 'COMPLETED'
          AND delivery_type = :deliveryType
    """, nativeQuery = true)
    Integer findMinDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    @Query(value = """
        SELECT MAX(TIMESTAMPDIFF(MINUTE, created_at, modified_at))
        FROM orders
        WHERE order_status = 'COMPLETED'
          AND delivery_type = :deliveryType
    """, nativeQuery = true)
    Integer findMaxDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    @Query(value = """
        SELECT AVG(TIMESTAMPDIFF(SECOND, created_at, modified_at))
        FROM orders
        WHERE order_status = 'COMPLETED'
          AND delivery_type = :deliveryType
    """, nativeQuery = true)
    Long findAvgDeliveryTimeByType(@Param("deliveryType") String deliveryType);

    Optional<Order> findByMemberAndOrderId(Member member, Long orderId);

    List<Order> findAllByStore_StoreId(Long storeStoreId);
}
