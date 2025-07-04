package com.idukbaduk.itseats.memberaddress.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    Optional<MemberAddress> findByMemberAndAddressId(Member member, Long addressId);

    List<MemberAddress> findAllByMember(Member member);
}
