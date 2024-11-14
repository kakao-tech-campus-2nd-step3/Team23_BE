package kappzzang.jeongsan.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.MemberKakaoIdResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestDataUtil.class)
class TeamMemberRepositoryTest {

    @Autowired
    private TestDataUtil testDataUtil;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team team;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        // given
        team = testDataUtil.createAndPersistTeam();

        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo();

        member1 = testDataUtil.createAndPersistMember("nickname1", kakaoPayInfo);
        member2 = testDataUtil.createAndPersistMember("nickname2", kakaoPayInfo);

        testDataUtil.createAndPersistTeamMember(member1, team, true, true);
        testDataUtil.createAndPersistTeamMember(member2, team, false, false);
    }

    @Test
    @DisplayName("모임 Id로 모임 멤버의 모임 초대 수락 여부를 조회한다")
    void findInvitationStatusByTeamId() {
        // when
        List<InvitationStatusResponse> result = teamMemberRepository.findInvitationStatusByTeamId(
            team.getId());

        // then
        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(response -> {
            assertThat(response.kakaoId()).isEqualTo(member1.getKakaoId());
            assertThat(response.nickname()).isEqualTo("nickname1");
            assertThat(response.isInviteAccepted()).isTrue();
        });

        assertThat(result).anySatisfy(response -> {
            assertThat(response.kakaoId()).isEqualTo(member2.getKakaoId());
            assertThat(response.nickname()).isEqualTo("nickname2");
            assertThat(response.isInviteAccepted()).isFalse();
        });
    }

    @Test
    @DisplayName("모임 Id로 멤버의 카카오 서비스 아이디를 조회한다")
    void findMemberKakaoIdByTeamId() {
        // when
        List<MemberKakaoIdResponse> result = teamMemberRepository.findMemberKakaoIdByTeamId(team.getId());

        // then
        assertThat(result).hasSize(2)
            .map(memberKakaoIdResponse -> memberKakaoIdResponse.id())
            .contains(member1.getKakaoId(), member2.getKakaoId());
    }
}
