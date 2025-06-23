package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.WorkingInfoResponse;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RiderService riderService;

    private final String username = "testuser";
    private Member member;
    private Rider rider;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        rider = Rider.builder()
                .member(member)
                .riderId(1L)
                .build();
    }

    @Test
    @DisplayName("출근 상태 전환 성공")
    void modifyWorking_true_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(true)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("퇴근 상태 전환 성공")
    void modifyWorking_false_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(false)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("존재하지 않는 라이더 조회시 예외 발생")
    void modifyWorking_notExistRider() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderService.modifyWorking(username, ModifyWorkingRequest.builder().build()))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_NOT_FOUND.getMessage());
    }
}
