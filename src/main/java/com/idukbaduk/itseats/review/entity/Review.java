package com.idukbaduk.itseats.review.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "store_star", nullable = false)
    private int storeStar;

    @Column(name = "rider_star")
    private Integer riderStar;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "menu_liked", nullable = false)
    private MenuLiked menuLiked;

    @Column(name = "content")
    private String content;

    @Column(name = "store_reply", nullable = false)
    private String storeReply;
}
