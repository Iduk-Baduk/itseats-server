package com.idukbaduk.itseats.rider.repository;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.RiderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderAssignmentRepository extends JpaRepository<RiderAssignment, Long> {

    @Query("""
        SELECT ra
        FROM RiderAssignment ra
        WHERE ra.rider.member.username = :username
          AND ra.order.orderId = :orderId
    """)
    Optional<RiderAssignment> findByUsernameAndOrderId(
            @Param("username") String username,
            @Param("orderId") Long orderId
    );

    Optional<RiderAssignment> findByRiderAndOrder(Rider rider, Order order);
}
