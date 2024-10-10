package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @InjectMocks
    private TeamService teamService;

    @Test
    @DisplayName("모임의 멤버 초대 현황 조회 - 모임을 찾을 수 없음")
    void getInvitationStatus_TeamNotFound() {
        // given
        given(teamRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getInvitationStatus(1L))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("모임의 멤버 초대 현황 조회 - 멤버 초대 기록을 찾을 수 없음")
    void getInvitationStatus_EmptyInvitationStatus() {
        // given
        Long teamId = 1L;
        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team()));
        given(teamMemberRepository.findInvitationStatusByTeamId(teamId)).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> teamService.getInvitationStatus(1L))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVITATION_STATUS_NOT_FOUND);
    }

    @Test
    @DisplayName("모임의 멤버 초대 현황 조회 - 초대 현황 조회 성공")
    void getInvitationStatus_InvitationStatusLoaded() {
        // given
        Long teamId = 1L;
        given(teamRepository.findById(teamId)).willReturn(Optional.of(new Team()));
        given(teamMemberRepository.findInvitationStatusByTeamId(teamId)).willReturn(
            List.of(new InvitationStatusResponse(1L, "nickname", "profileImage", false))
        );

        // when
        List<InvitationStatusResponse> result = teamService.getInvitationStatus(teamId);

        // then
        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .first()
            .satisfies(response -> {
                assertThat(response.nickname()).isEqualTo("nickname");
                assertThat(response.profileImage()).isEqualTo("profileImage");
                assertThat(response.isInviteAccepted()).isFalse();
            });
    }
}
