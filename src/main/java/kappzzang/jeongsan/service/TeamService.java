package kappzzang.jeongsan.service;

import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<TeamResponse> getTeamsByIsClosed(Boolean isClosed) {
        return teamRepository.findByIsClosed(isClosed)
                .stream()
                .map(TeamResponse::from)
                .toList();
    }
}
