package com.idukbaduk.itseats.memberaddress.repository;

import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
}
