package kappzzang.jeongsan.service;

import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByIsClosed(Boolean isClosed) {
        return teamRepository.findByIsClosed(isClosed)
                .stream()
                .map(TeamResponse::from)
                .toList();
    }
}
