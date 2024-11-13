package kappzzang.jeongsan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TeamTest {

    @Test
    @DisplayName("모임 생성 시 팀원이 없는 경우 테스트")
    void createTeamWithoutMembers() {
        // given
        Member owner = createOwnerMember();

        // when
        Team team = Team.createTeam(owner, "TeamName", "❤", Collections.emptyList());

        // then
        assertThat(team.getName()).isEqualTo("TeamName");
        assertThat(team.getTeamMemberList()).hasSize(1);
        assertThat(team.getOwnerKakaoId()).isEqualTo("ownerKakaoId");
        assertThat(team.getSubject()).isEqualTo("❤");
        assertThat(team.getIsClosed()).isFalse();
    }

    @Test
    @DisplayName("모임 생성 시 일반적인 경우 테스트")
    void createTeamWithMembers() {
        // given
        Member owner = createOwnerMember();
        Member member1 = createMember("member1KakaoId");
        Member member2 = createMember("member2KakaoId");

        // when
        Team team = Team.createTeam(owner, "test team", "⚽", List.of(member1, member2));

        // then
        assertThat(team.getName()).isEqualTo("test team");
        assertThat(team.getTeamMemberList()).hasSize(3);
        assertThat(team.getOwnerKakaoId()).isEqualTo("ownerKakaoId");

        assertThat(team.getTeamMemberList()).anyMatch(
            tm -> tm.getMember().getKakaoId().equals("member1KakaoId"));
        assertThat(team.getTeamMemberList()).anyMatch(
            tm -> tm.getMember().getKakaoId().equals("member2KakaoId"));

        assertThat(team.getTeamMemberList()).anyMatch(
            tm -> tm.getMember().getKakaoId().equals("ownerKakaoId") && tm.getIsOwner());
        assertThat(team.getTeamMemberList()).anyMatch(
            tm -> tm.getMember().getKakaoId().equals("member1KakaoId") && !tm.getIsOwner());
        assertThat(team.getTeamMemberList()).anyMatch(
            tm -> tm.getMember().getKakaoId().equals("member2KakaoId") && !tm.getIsOwner());
    }

    @Test
    @DisplayName("팀을 종료할 때 소유자가 아닌 사용자가 종료를 시도할 경우 예외 발생")
    void closeTeamByNonOwnerThrowsException() {
        // given
        Member owner = createOwnerMember();
        Member nonOwner = createMember("nonOwnerKakaoId");
        Team team = Team.createTeam(owner, "test team", "🎈", List.of(nonOwner));

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(nonOwner, "id", 2L);

        // when & then
        assertThatThrownBy(() -> team.closeTeam(nonOwner.getId()))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.TEAM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 종료된 팀을 종료 시도할 때 예외 발생")
    void closeAlreadyClosedTeamThrowsException() {
        // given
        Member owner = createOwnerMember();
        Team team = Team.createTeam(owner, "test team", "😀", Collections.emptyList());
        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(team, "isClosed", true);

        // when & then
        assertThatThrownBy(() -> team.closeTeam(owner.getId()))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.TEAM_ALREADY_CLOSED.getMessage());
    }

    @Test
    @DisplayName("모임 종료 성공")
    void closeTeamSetsIsClosedToTrue() {
        // given
        Member owner = createOwnerMember();
        Team team = Team.createTeam(owner, "test team", "😀", Collections.emptyList());
        ReflectionTestUtils.setField(owner, "id", 1L);

        // when
        team.closeTeam(owner.getId());

        // then
        assertThat(team.getIsClosed()).isTrue();
    }

    @Test
    @DisplayName("팀에 특정 멤버가 포함되어 있는지 확인")
    void isMember() {
        // given
        Member owner = createOwnerMember();
        Member member = createMember("memberKakaoId");
        Member nonMember = createMember("notMemberKakaoId");
        Team team = Team.createTeam(owner, "test team", "🎇", List.of(member));

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(nonMember, "id", 3L);

        team.getTeamMemberList().forEach(teamMember -> {
            if (teamMember.getMember().equals(member)) {
                ReflectionTestUtils.setField(teamMember, "isInviteAccepted", true);
            }
        });

        // when & then
        assertThat(team.isMember(owner)).isTrue();
        assertThat(team.isMember(member)).isTrue();
        assertThat(team.isMember(nonMember)).isFalse();
    }

    @Test
    @DisplayName("팀에 특정 멤버가 초대를 수락하지 않았을 때 예외 발생")
    void isMemberNotAccepted() {
        // given
        Member owner = createOwnerMember();
        Member member = createMember("memberKakaoId");
        Member notAcceptedMember = createMember("notAcceptedMemberKakaoId");
        Team team = Team.createTeam(owner, "test team", "🎇", List.of(member));

        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(notAcceptedMember, "id", 3L);

        team.getTeamMemberList().forEach(teamMember -> {
            if (teamMember.getMember().equals(notAcceptedMember)) {
                ReflectionTestUtils.setField(teamMember, "isInviteAccepted", false);
            } else {
                ReflectionTestUtils.setField(teamMember, "isInviteAccepted", true);
            }
        });

        // when & then
        assertThat(team.isMember(owner)).isTrue();
        assertThat(team.isMember(member)).isTrue();
        assertThat(team.isMember(notAcceptedMember)).isFalse();
    }

    @Test
    @DisplayName("팀 소유자가 없는 경우 getOwnerKakaoId 호출 시 예외 발생")
    void getOwnerKakaoIdThrowsExceptionWhenNoOwner() {
        // given
        Team team = new Team("test team", "😀");

        // when & then
        assertThatThrownBy(team::getOwnerKakaoId)
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.TEAM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("팀 소유자의 Kakao ID 조회")
    void getOwnerKakaoId() {
        // given
        Member owner = createOwnerMember();
        Team team = Team.createTeam(owner, "test team", "🎈", Collections.emptyList());

        // when
        String kakaoId = team.getOwnerKakaoId();

        // then
        assertThat(kakaoId).isEqualTo("ownerKakaoId");
    }

    @Test
    @DisplayName("팀에 멤버 추가")
    void addMember() {
        // given
        Member owner = createOwnerMember();
        Member newMember = createMember("newMemberKakaoId");
        Team team = Team.createTeam(owner, "test team", "🎈", Collections.emptyList());

        // when
        team.addMember(newMember, false, false);

        // then
        assertThat(team.getTeamMemberList()).hasSize(2);
        assertThat(team.getTeamMemberList()).anyMatch(tm -> tm.getMember().equals(newMember));
    }

    private Member createOwnerMember() {
        return createMember("ownerKakaoId");
    }

    private Member createMember(String kakaoId) {
        return Member.builder().kakaoId(kakaoId).build();
    }
}
