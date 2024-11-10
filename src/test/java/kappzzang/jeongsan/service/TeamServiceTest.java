package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.CreateTeamRequest;
import kappzzang.jeongsan.dto.response.CreateTeamResponse;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.MemberIdResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.MemberRepository;
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
    @Mock
    private MemberRepository memberRepository;
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
        given(teamMemberRepository.findInvitationStatusByTeamId(teamId)).willReturn(
            Collections.emptyList());

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
            List.of(new InvitationStatusResponse("kakaoId", "nickname", "profileImage", false))
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

    @Test
    @DisplayName("teamId로 모임을 찾을 수 없어서 모임 멤버 아이디 조회 실패함")
    void getMemberId_TeamNotFound() {
        // given
        given(teamRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getMemberId(anyLong()))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("해당 모임에 멤버가 없어서 모임 멤버 아이디 조회 실패함")
    void getMemberId_TeamMemberNotFound() {
        // given
        given(teamRepository.findById(anyLong())).willReturn(Optional.of(new Team()));
        given(teamMemberRepository.findMemberIdByTeamId(anyLong())).willReturn(
            Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> teamService.getMemberId(anyLong()))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.TEAM_MEMBER_NOT_FOUND);
    }

    // 성공
    @Test
    @DisplayName("모임 멤버 아이디 조회 성공")
    void getMemberId_MemberIdLoaded() {
        // given
        given(teamRepository.findById(anyLong())).willReturn(Optional.of(new Team()));
        given(teamMemberRepository.findMemberIdByTeamId(anyLong())).willReturn(
            List.of(new MemberIdResponse(1L)));

        // when
        List<MemberIdResponse> result = teamService.getMemberId(anyLong());

        // then
        assertThat(result)
            .isNotNull()
            .hasSize(1)
            .first()
            .satisfies(response -> {
                assertThat(response.id()).isEqualTo(1L);
            });
    }

    @Test
    @DisplayName("모임 생성 성공")
    void createTeam_Success() {
        // given
        Long ownerId = 1L;
        Member owner = new Member();
        String memberKakaoId1 = "memberKakaoId1";
        Member member1 = new Member();
        String memberKakaoId2 = "memberKakaoId2";
        Member member2 = new Member();
        String teamName = "Test Team";
        CreateTeamRequest request = new CreateTeamRequest(teamName, "subject", new ArrayList<>(
            Arrays.asList(memberKakaoId1, memberKakaoId2)));
        List<Member> members = new ArrayList<>(Arrays.asList(member1, member2));
        Team team = Team.createTeam(owner, teamName, "subject", members);

        given(teamRepository.save(any(Team.class))).willReturn(team);
        given(memberRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(memberRepository.findByKakaoId(memberKakaoId1)).willReturn(Optional.of(member1));
        given(memberRepository.findByKakaoId(memberKakaoId2)).willReturn(Optional.of(member2));

        // when
        CreateTeamResponse actual = teamService.createTeam(ownerId, request);

        // then
        assertThat(actual).isNotNull();

        then(teamRepository).should().save(any(Team.class));
        then(teamRepository).shouldHaveNoMoreInteractions();

        then(memberRepository).should().findById(ownerId);
        then(memberRepository).should().findByKakaoId(memberKakaoId1);
        then(memberRepository).should().findByKakaoId(memberKakaoId2);
        then(memberRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("잘못된 teamId를 이용한 조회로 notfound 발생")
    void getTeam_NotFound() {
        // given
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getTeam(1L))
            .isInstanceOf(JeongsanException.class).hasMessageContaining("찾을 수 없습니다.");
        then(teamRepository).should().findById(any(Long.class));
    }

    @Test
    @DisplayName("teamId를 이용한 조회 성공")
    void getTeam_success() {
        // given
        String teamName = "Test Team";
        Team team = mock(Team.class);

        given(team.getId()).willReturn(1L);
        given(team.getName()).willReturn(teamName);
        given(team.getIsClosed()).willReturn(false);
        given(team.getSubject()).willReturn("subject");
        given(team.getTeamMemberList()).willReturn(Collections.emptyList());
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        // when
        TeamResponse actual = teamService.getTeam(1L);

        // then
        assertThat(actual).isNotNull();
        then(teamRepository).should().findById(1L);
    }
}
