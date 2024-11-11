package kappzzang.jeongsan.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestDataUtil.class)
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TestDataUtil testDataUtil;

    private Member member;
    private Team openTeam;
    private Team closedTeam;

    @BeforeEach
    void setUp() {
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo();
        member = testDataUtil.createAndPersistMember("TestUser", kakaoPayInfo);

        openTeam = testDataUtil.createAndPersistTeam();
        openTeam.setClosed(false);
        testDataUtil.createAndPersistTeamMember(member, openTeam, true, true);

        closedTeam = testDataUtil.createAndPersistTeam();
        closedTeam.setClosed(true);
        testDataUtil.createAndPersistTeamMember(member, closedTeam, false, true);

        testDataUtil.commit();
        testDataUtil.clear();
    }

    @Test
    @DisplayName("모임의 isClosed `false`상태인 데이터를 불러온다")
    void testFindByIsClosed_withOpenTeam() {
        // given
        Boolean isClosed = false;

        // when
        List<Team> result = teamRepository.findByIsClosed(member.getId(), isClosed);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(openTeam);
    }

    @Test
    @DisplayName("모임의 isClosed가 `true`상태인 데이터를 불러온다")
    void testFindByIsClosed_withClosedTeam() {
        // given
        Boolean isClosed = true;

        // when
        List<Team> result = teamRepository.findByIsClosed(member.getId(), isClosed);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(closedTeam);
    }

    @Test
    @DisplayName("멤버가 속한 팀이 없을 때 데이터를 요청하면 빈 리스트를 반환한다")
    void testFindByIsClosed_noMatchingTeam() {
        // given
        Long nonExistingMemberId = -1L;
        Boolean isClosed = false;

        // when
        List<Team> result = teamRepository.findByIsClosed(nonExistingMemberId, isClosed);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("JPQL을 이용한 요청에 대하여 지연 로딩이 정상적으로 적용되었는지 확인")
    void testFindByIsClosed_LazyLoadingOfTeamMembersAndMembers() {
        // given
        Boolean isClosed = false;

        // when
        List<Team> result = teamRepository.findByIsClosed(member.getId(), isClosed);

        // then
        assertThat(result).hasSize(1);
        Team retrievedTeam = result.getFirst();

        
        assertThat(retrievedTeam.getTeamMemberList()).isNotEmpty();

        TeamMember teamMember = retrievedTeam.getTeamMemberList().getFirst();
        assertThat(teamMember.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("지연로딩을 이용한 Team.getOwnerKakaoId()를 정상적으로 수행하는지 확인하는 테스트")
    void testGetOwnerKakaoId_withLazyLoading() {
        // given
        Boolean isClosed = false;

        // when
        List<Team> result = teamRepository.findByIsClosed(member.getId(), isClosed);

        // then
        assertThat(result).hasSize(1);
        Team retrievedTeam = result.getFirst();

        String ownerKakaoId = retrievedTeam.getOwnerKakaoId();
        assertThat(ownerKakaoId).isEqualTo(member.getKakaoId());
    }
}
