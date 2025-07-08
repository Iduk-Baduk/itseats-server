package com.idukbaduk.itseats.payment.repository;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.member.username = :username
    """)
    Optional<Payment> findByUsername(@Param("username") String username);
}
