package kappzzang.jeongsan.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TeamTest {

    @Test
    @DisplayName("모임 생성 시 팀원이 없는 경우 테스트")
    void createTeamWithoutMembers() {
        // given
        Member owner = Member.builder()
            .kakaoId("ownerKakaoId")
            .build();

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
        Member owner = Member.builder().kakaoId("ownerKakaoId").build();
        Member member1 = Member.builder().kakaoId("member1KakaoId").build();
        Member member2 = Member.builder().kakaoId("member2KakaoId").build();

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

}
