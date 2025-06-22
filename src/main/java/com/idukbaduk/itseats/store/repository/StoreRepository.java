package com.idukbaduk.itseats.store.repository;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByMemberAndStoreId(Member member, Long storeId);

    List<Store> findAllByDeletedFalse();

    Optional<Store> findByStoreIdAndDeletedFalse(Long storeId);
}
