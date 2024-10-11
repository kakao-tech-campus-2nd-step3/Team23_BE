package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

    @Test
    @DisplayName("모임 생성자에게 동일한 모임 이름이 존재")
    void createTeam_NameDuplicateException() {
        // given
        Long memberId = 1L;
        String teamName = "Test Team";
        given(teamRepository.existsByNameAndMemberId(teamName, memberId)).willReturn(true);

        // when & then
        assertThatThrownBy(() ->
            teamService.createTeam(memberId,
                new CreateTeamRequest(teamName, "subject", new ArrayList<>()))
        ).isInstanceOf(JeongsanException.class);
        then(teamRepository).should().existsByNameAndMemberId(teamName, memberId);
    }

    @Test
    @DisplayName("모임 생성 성공")
    void createTeam_Success() {
        // given
        Long ownerId = 1L;
        Member owner = new Member();
        Long memberId1 = 2L;
        Member member1 = new Member();
        Long memberId2 = 3L;
        Member member2 = new Member();
        String teamName = "Test Team";
        CreateTeamRequest request = new CreateTeamRequest(teamName, "subject", new ArrayList<>(
            Arrays.asList(memberId1, memberId2)));
        List<Member> members = new ArrayList<>(Arrays.asList(member1, member2));
        Team team = Team.createTeam(owner, teamName, "subject", members);

        given(teamRepository.existsByNameAndMemberId(teamName, ownerId)).willReturn(false);
        given(teamRepository.save(any(Team.class))).willReturn(team);
        given(memberRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(memberRepository.findById(memberId1)).willReturn(Optional.of(member1));
        given(memberRepository.findById(memberId2)).willReturn(Optional.of(member2));

        // when
        CreateTeamResponse actual = teamService.createTeam(ownerId, request);

        // then
        assertThat(actual).isNotNull();

        then(teamRepository).should().existsByNameAndMemberId(teamName, ownerId);
        then(teamRepository).should().save(any(Team.class));
        then(teamRepository).shouldHaveNoMoreInteractions();

        then(memberRepository).should().findById(ownerId);
        then(memberRepository).should().findById(memberId1);
        then(memberRepository).should().findById(memberId2);
        then(memberRepository).shouldHaveNoMoreInteractions();
    }
}
