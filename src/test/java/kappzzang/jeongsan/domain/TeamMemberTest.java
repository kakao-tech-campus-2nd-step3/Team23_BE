package kappzzang.jeongsan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class TeamMemberTest {

    @Mock
    Member mockMember;
    @Mock
    Team mockTeam;

    @Test
    @DisplayName("모임 초대를 수락할 수 있다")
    void acceptInvitation() {
        // given
        TeamMember teamMember = new TeamMember(mockMember, mockTeam, false, false);

        // when
        teamMember.acceptInvite();

        // then
        assertThat(teamMember.getIsInviteAccepted()).isEqualTo(true);
    }

    @Test
    @DisplayName("이미 초대를 수락한 멤버가 요청 수락을 시도하면 예외가 발생한다")
    void acceptInvite_throwsExceptionIfAlreadyAccepted() {
        // given
        TeamMember teamMember = new TeamMember(mockMember, mockTeam, false, true);

        // when, then
        assertThatThrownBy(teamMember::acceptInvite).isInstanceOf(JeongsanException.class)
            .hasMessageContaining(
                ErrorType.ALREADY_JOINED_MEMBER.getMessage());
    }
}
