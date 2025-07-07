package com.idukbaduk.itseats.member.repository;

import com.idukbaduk.itseats.member.entity.Favorite;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByMemberAndStore(Member member, Store store);

    int countByMember(Member member);
}
