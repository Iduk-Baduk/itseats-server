package com.idukbaduk.itseats.review.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "store_star", nullable = false)
    private int storeStar;

    @Column(name = "rider_star")
    private int riderStar;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "menu_liked", nullable = false)
    private MenuLiked menuLiked;

    @Column(name = "content")
    private String content;

    @Column(name = "store_reply", nullable = false)
    private String storeReply;
}
