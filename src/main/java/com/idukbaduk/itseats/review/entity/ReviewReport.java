package com.idukbaduk.itseats.review.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.review.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review_report")
public class ReviewReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    public static ReviewReport create(Member member, Review review, String reason) {
        return ReviewReport.builder()
                .member(member)
                .review(review)
                .reason(reason)
                .reportStatus(ReportStatus.ACCEPTED)
                .build();
    }
}
