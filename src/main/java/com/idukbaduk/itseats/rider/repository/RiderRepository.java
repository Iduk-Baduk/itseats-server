package com.idukbaduk.itseats.rider.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.rider.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {

    Optional<Rider> findByMember(Member member);

    @Query("""
        SELECT r
        FROM Rider r
        WHERE r.member.username = :username
    """)
    Optional<Rider> findByUsername(@Param("username") String username);
}
