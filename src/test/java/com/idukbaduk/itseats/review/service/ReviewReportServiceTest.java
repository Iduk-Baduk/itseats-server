package com.idukbaduk.itseats.review.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.review.dto.ReviewReportRequest;
import com.idukbaduk.itseats.review.dto.ReviewReportResponse;
import com.idukbaduk.itseats.review.entity.Review;
import com.idukbaduk.itseats.review.entity.ReviewReport;
import com.idukbaduk.itseats.review.entity.enums.ReportStatus;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.review.repository.ReviewReportRepository;
import com.idukbaduk.itseats.review.repository.ReviewRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewReportServiceTest {

    @Mock
    private ReviewReportRepository reviewReportRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private OwnerReviewService ownerReviewService;

    @Test
    @DisplayName("리뷰 신고 성공")
    void reportReview_success() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        Member member = Member.builder().memberId(1L).username(username).build();
        Store store = Store.builder().storeId(storeId).member(member).build();
        Review review = Review.builder().reviewId(reviewId).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewReportRepository.existsByMember_MemberIdAndReview_ReviewId(member.getMemberId(), reviewId)).willReturn(false);
        given(reviewReportRepository.save(any(ReviewReport.class)))
                .willAnswer(invocation -> {
                    ReviewReport saved = invocation.getArgument(0);
                    return ReviewReport.builder()
                            .reportId(999L)
                            .member(saved.getMember())
                            .review(saved.getReview())
                            .reason(saved.getReason())
                            .reportStatus(saved.getReportStatus())
                            .build();
                });

        // when
        ReviewReportResponse response = ownerReviewService.reportReview(username, storeId, reviewId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReporter()).isEqualTo(username);
        assertThat(response.getReviewId()).isEqualTo(reviewId);
        assertThat(response.getReason()).isEqualTo("욕설");
        assertThat(response.getReportStatus()).isEqualTo(ReportStatus.ACCEPTED);
    }

    @Test
    @DisplayName("가맹점주가 아니면 예외 발생")
    void reportReview_notStoreOwner() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        Member member = Member.builder().memberId(1L).username(username).build();
        Member otherMember = Member.builder().memberId(2L).username("other").build();
        Store store = Store.builder().storeId(storeId).member(otherMember).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> ownerReviewService.reportReview(username, storeId, reviewId, request))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.NOT_STORE_OWNER.getMessage());
    }

    @Test
    @DisplayName("리뷰가 없으면 예외 발생")
    void reportReview_reviewNotFound() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        Member member = Member.builder().memberId(1L).username(username).build();
        Store store = Store.builder().storeId(storeId).member(member).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerReviewService.reportReview(username, storeId, reviewId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessageContaining(ReviewErrorCode.REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("중복 신고 시 예외 발생")
    void reportReview_alreadyReported() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        Member member = Member.builder().memberId(1L).username(username).build();
        Store store = Store.builder().storeId(storeId).member(member).build();
        Review review = Review.builder().reviewId(reviewId).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewReportRepository.existsByMember_MemberIdAndReview_ReviewId(member.getMemberId(), reviewId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> ownerReviewService.reportReview(username, storeId, reviewId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessageContaining(ReviewErrorCode.ALREADY_REPORTED.getMessage());
    }

    @Test
    @DisplayName("신고자(멤버) 정보가 없으면 예외 발생")
    void reportReview_memberNotFound() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerReviewService.reportReview(username, storeId, reviewId, request))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("가맹점 정보가 없으면 예외 발생")
    void reportReview_storeNotFound() {
        // given
        String username = "owner1";
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        Member member = Member.builder().memberId(1L).username(username).build();
        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerReviewService.reportReview(username, storeId, reviewId, request))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }
}
