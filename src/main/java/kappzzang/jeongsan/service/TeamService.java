package kappzzang.jeongsan.service;

import java.util.List;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.CloseTeamRequest;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

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
}
