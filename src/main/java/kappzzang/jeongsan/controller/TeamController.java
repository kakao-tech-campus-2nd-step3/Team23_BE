package kappzzang.jeongsan.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kappzzang.jeongsan.dto.request.CloseTeamRequest;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<JeongsanApiResponse<List<TeamResponse>>> getTeams(@RequestParam("isClosed") Boolean isClosed) {
        List<TeamResponse> data = teamService.getTeamsByIsClosed(isClosed);

        return JeongsanApiResponse.success(SuccessType.TEAM_LIST_LOADED, data);
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> closeTeam(
        @PathVariable("teamId") Long teamId, @RequestBody CloseTeamRequest request) {
        teamService.closeTeam(teamId, request);
        return JeongsanApiResponse.success(SuccessType.TEAM_CLOSED);
    }
}
