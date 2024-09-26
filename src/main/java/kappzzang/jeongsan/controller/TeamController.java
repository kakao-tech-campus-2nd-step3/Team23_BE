package kappzzang.jeongsan.controller;

import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams(@RequestParam("isClosed") Boolean isClosed) {
        List<TeamResponse> data = teamService.getTeamsByIsClosed(isClosed);

        return ResponseEntity.ok(data);
    }
}
