package com.idukbaduk.itseats.order.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.dto.NearbyOrderDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.rider.entity.Rider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(MINUTE, cook_start_time, order_end_time))
            FROM orders
            WHERE store_id = :storeId
            AND cook_start_time IS NOT NULL
            AND order_end_time IS NOT NULL
            """, nativeQuery = true)
    Double findAverageCookTimeByStoreId(@Param("storeId") Long storeId);

    @Query(value = """ 
            SELECT COUNT(*)
            FROM orders
            WHERE store_id = :storeId
            AND order_end_time IS NOT NULL
            AND delivery_eta IS NOT NULL
            AND ABS(TIMESTAMPDIFF(MINUTE, delivery_eta, order_end_time)) <= 5
            """, nativeQuery = true)
    Long countAccurateOrdersByStoreId(@Param("storeId") Long storeId);

    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(MINUTE, order_received_time, order_end_time))
            FROM orders
            WHERE store_id = :storeId
            AND order_received_time IS NOT NULL
            AND order_end_time IS NOT NULL
            """ , nativeQuery = true)
    Double findAveragePickupTimeByStoreId(@Param("storeId") Long storeId);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE store_id = :storeId",
            nativeQuery = true)
    Long countTotalOrdersByStoreId(@Param("storeId") Long storeId);

    @Query(value = """
            SELECT COUNT(*)
            FROM orders WHERE store_id = :storeId AND order_status
            IN ('COOKING', 'RIDER_READY', 'DELIVERING', 'DELIVERED', 'COMPLETED')
            """, nativeQuery = true)
    Long countAcceptedOrdersByStoreId(@Param("storeId") Long storeId);

    @Query("""
        SELECT o FROM Order o
          JOIN FETCH o.member m
          JOIN FETCH o.orderMenus om
          JOIN FETCH om.menu menu
        WHERE o.store.member.username = :username
          AND o.orderId = :orderId
    """)
    Optional<Order> findDetailByStoreUsernameAndId(
            @Param("username") String username,
            @Param("orderId") Long orderId
    );

    @Query("""
        SELECT distinct o
        FROM Order o
        JOIN FETCH o.orderMenus om
        JOIN FETCH om.menu m
        WHERE o.store.member.username = :username
          AND o.store.storeId = :storeId
    """)
    List<Order> findAllWithMenusByStoreUsernameAndStoreId(
            @Param("username") String username,
            @Param("storeId") Long storeId
    );

    Optional<Order> findByStoreMemberUsernameAndOrderId(String username, Long orderId);

    Optional<Order> findByRiderAndOrderId(Rider rider, Long orderId);

    @Query(value = """
        SELECT o.*
        FROM orders o
        JOIN store s ON o.store_id = s.store_id
        WHERE o.order_status = "COOKED"
        ORDER BY ST_Distance_Sphere(
            s.location,
            ST_GeomFromText(CONCAT('POINT(', :riderLng, ' ', :riderLat, ')'))
        ), o.order_id
        LIMIT 1
    """, nativeQuery = true)
    Optional<Order> findCookedOrderByRiderLocation(
            @Param("riderLat") double riderLat,
            @Param("riderLng") double riderLng
    );

    @EntityGraph(attributePaths = {"store", "payment"})
    @Query("""
        SELECT o
        FROM Order o
        WHERE o.member.username = :username
          AND (
            :keyword = '' OR
            EXISTS (
              SELECT 1 FROM OrderMenu om
              WHERE om.order = o AND om.menuName LIKE %:keyword%
            ) OR
            o.store.storeName LIKE %:keyword%
          )
        ORDER BY o.createdAt DESC
    """)
    Slice<Order> findOrdersByUsernameWithKeyword(String username, String keyword, Pageable pageable);

    @Query(value = """
            SELECT  order_id,
                    store_name,
                    distance,
                    delivery_fee,
                    delivery_address, 
                    delivery_type
            FROM (
                SELECT  o.order_id,
                        s.store_name,
                        o.delivery_fee,
                        o.delivery_address,
                        o.delivery_type,
                        ST_DISTANCE_SPHERE(
                            ST_PointFromTEXT(CONCAT('POINT(', :lon, ' ', :lat, ')'), 4326),
                            s.location
                        ) AS distance
                FROM orders o
                    JOIN store s ON o.store_id = s.store_id
                WHERE o.order_status = 'COOKED') AS nearbyOrders
            WHERE distance <= :radiusInMeters
            ORDER BY distance;
    """, nativeQuery = true)
    List<NearbyOrderDTO> findNearbyOrders(
            @Param("lat") double latitude,
            @Param("lon") double longitude,
            @Param("radiusInMeters") int radiusInMeters
    );
}
