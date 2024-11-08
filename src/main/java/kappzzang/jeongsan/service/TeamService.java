package kappzzang.jeongsan.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.CreateTeamRequest;
import kappzzang.jeongsan.dto.response.CreateTeamResponse;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByIsClosed(Boolean isClosed) {
        return teamRepository.findByIsClosed(isClosed)
            .stream()
            .map(TeamResponse::from)
            .toList();
    }

    @Transactional
    public CreateTeamResponse createTeam(Long memberId, CreateTeamRequest request) {
        if (teamRepository.existsByNameAndMemberId(request.name(), memberId)) {
            throw new JeongsanException(ErrorType.TEAM_NAME_DUPLICATED);
        }

        Member owner = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));

        List<Member> members = Collections.emptyList();
        if (!request.members().isEmpty()) {
            members = request.members().stream()
                .map(id -> memberRepository.findById(id)
                    .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND)))
                .toList();
        }

        Team team = Team.createTeam(owner, request.name(), request.subject(), members);

        return new CreateTeamResponse(teamRepository.save(team).getId());
    }

    @Transactional
    public void closeTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        team.closeTeam();
    }

    public List<InvitationStatusResponse> getInvitationStatus(Long teamId) {
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));

        return Optional.ofNullable(teamMemberRepository.findInvitationStatusByTeamId(teamId))
            .filter(list -> !list.isEmpty())
            .orElseThrow(() -> new JeongsanException(ErrorType.INVITATION_STATUS_NOT_FOUND));
    }
}
