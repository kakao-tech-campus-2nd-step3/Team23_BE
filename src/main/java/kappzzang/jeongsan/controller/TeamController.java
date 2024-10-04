package kappzzang.jeongsan.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import kappzzang.jeongsan.controller.docs.TeamControllerInterface;
import kappzzang.jeongsan.dto.request.CloseTeamRequest;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController implements TeamControllerInterface {

    private final TeamService teamService;

    @Override
    @GetMapping
    public ResponseEntity<JeongsanApiResponse<List<TeamResponse>>> getTeams(
        @RequestParam("isClosed") Boolean isClosed) {
        List<TeamResponse> data = teamService.getTeamsByIsClosed(isClosed);

        return JeongsanApiResponse.success(SuccessType.TEAM_LIST_LOADED, data);
    }

    @Override
    @PatchMapping("/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> closeTeam(
        @PathVariable("teamId") Long teamId, @RequestBody CloseTeamRequest request) {
        teamService.closeTeam(teamId, request);
        return JeongsanApiResponse.success(SuccessType.TEAM_CLOSED);
    }
}
