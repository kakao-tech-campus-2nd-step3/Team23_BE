package kappzzang.jeongsan.repository;

import kappzzang.jeongsan.domain.KakaoToken;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team team;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        // given
        team = new Team();
        entityManager.persist(team);

        KakaoToken kakaoToken = new KakaoToken();
        entityManager.persist(kakaoToken);

        member1 = new Member(1L, "user1", "nickname1", "profile1", "token1", kakaoToken, List.of(new TeamMember()));
        member2 = new Member(2L, "user2", "nickname2", "profile2", "token2", kakaoToken, List.of(new TeamMember()));
        entityManager.persist(member1);
        entityManager.persist(member2);

        TeamMember teamMember1 = new TeamMember(1L, member1, team,  true, true);
        TeamMember teamMember2 = new TeamMember(2L, member2, team,  false, false);
        entityManager.merge(teamMember1);
        entityManager.merge(teamMember2);

        entityManager.flush();
    }

    @Test
    void findInvitationStatusByTeamId_ShouldReturnCorrectList() {
        // when
        List<InvitationStatusResponse> result = teamMemberRepository.findInvitationStatusByTeamId(team.getId());

        // then
        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(response -> {
            assertThat(response.memberId()).isEqualTo(member1.getId());
            assertThat(response.nickname()).isEqualTo("nickname1");
            assertThat(response.profileImage()).isEqualTo("profile1");
            assertThat(response.isInviteAccepted()).isTrue();
        });

        assertThat(result).anySatisfy(response -> {
            assertThat(response.memberId()).isEqualTo(member2.getId());
            assertThat(response.nickname()).isEqualTo("nickname2");
            assertThat(response.profileImage()).isEqualTo("profile2");
            assertThat(response.isInviteAccepted()).isFalse();
        });
    }
}
