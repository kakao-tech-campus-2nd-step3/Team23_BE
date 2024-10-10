package kappzzang.jeongsan.service;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.CloseTeamRequest;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
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

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByIsClosed(Boolean isClosed) {
        return teamRepository.findByIsClosed(isClosed)
            .stream()
            .map(TeamResponse::from)
            .toList();
    }

    @Transactional
    public void closeTeam(Long teamId, CloseTeamRequest request) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        team.closeTeam(request.isClosed());
    }

    public List<InvitationStatusResponse> getInvitationStatus(Long teamId) {
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));

        return Optional.ofNullable(teamMemberRepository.findInvitationStatusByTeamId(teamId))
            .filter(list -> !list.isEmpty())
            .orElseThrow(() -> new JeongsanException(ErrorType.INVITATION_STATUS_NOT_FOUND));
    }
}
