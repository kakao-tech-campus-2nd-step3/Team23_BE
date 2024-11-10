package kappzzang.jeongsan.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.util.List;
import kappzzang.jeongsan.controller.docs.TeamControllerInterface;
import kappzzang.jeongsan.dto.request.CreateTeamRequest;
import kappzzang.jeongsan.dto.request.TransferTargetRequest;
import kappzzang.jeongsan.dto.response.CreateTeamResponse;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.MemberKakaoIdResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.dto.response.TransferTargetResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @RequestParam("isClosed") Boolean isClosed,
        @AuthenticationPrincipal Long memberId) {
        List<TeamResponse> data = teamService.getTeamsByIsClosed(isClosed, memberId);

        return JeongsanApiResponse.success(SuccessType.TEAM_LIST_LOADED, data);
    }

    @Override
    @GetMapping("{teamId}")
    public ResponseEntity<JeongsanApiResponse<TeamResponse>> getTeam(@PathVariable Long teamId,
        @AuthenticationPrincipal Long memberId) {
        TeamResponse data = teamService.getTeam(teamId, memberId);
        return JeongsanApiResponse.success(SuccessType.TEAM_LIST_LOADED, data);
    }

    @Override
    @GetMapping("{teamId}")
    public ResponseEntity<JeongsanApiResponse<TeamResponse>> getTeam(@PathVariable Long teamId) {
        TeamResponse data = teamService.getTeam(teamId);
        return JeongsanApiResponse.success(SuccessType.TEAM_LIST_LOADED, data);
    }

    @Override
    @PostMapping
    public ResponseEntity<JeongsanApiResponse<CreateTeamResponse>> createTeam(
        @AuthenticationPrincipal Long memberId, @Valid @RequestBody CreateTeamRequest request) {
        CreateTeamResponse data = teamService.createTeam(memberId, request);
        return JeongsanApiResponse.success(SuccessType.TEAM_CREATED, data);
    }

    @Override
    @PatchMapping("/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> closeTeam(
        @PathVariable("teamId") Long teamId,
        @AuthenticationPrincipal Long memberId) {
        teamService.closeTeam(teamId, memberId);
        return JeongsanApiResponse.success(SuccessType.TEAM_CLOSED);
    }

    @Override
    @GetMapping("/{teamId}/members")
    public ResponseEntity<JeongsanApiResponse<List<InvitationStatusResponse>>> getInvitationStatus(
        @PathVariable("teamId") Long teamId) {
        List<InvitationStatusResponse> data = teamService.getInvitationStatus(teamId);
        return JeongsanApiResponse.success(SuccessType.INVITATION_STATUS_LOADED, data);
    }

    @Override
    @GetMapping("/{teamId}/members/id")
    public ResponseEntity<JeongsanApiResponse<List<MemberKakaoIdResponse>>> getMemberKakaoId(
        @PathVariable("teamId") Long teamId) {
        List<MemberKakaoIdResponse> data = teamService.getMemberKakaoId(teamId);
        return JeongsanApiResponse.success(SuccessType.MEMBER_KAKAO_ID_LOADED, data);
    }

    @Override
    @PostMapping("/{teamId}/transfers")
    public ResponseEntity<JeongsanApiResponse<List<TransferTargetResponse>>> getTransferTargetList(
        @AuthenticationPrincipal Long memberId, @PathVariable("teamId") Long teamId,
        @Valid @RequestBody TransferTargetRequest request) {
        List<TransferTargetResponse> data =
            teamService.getTransferTargetList(memberId, teamId, request);
        return JeongsanApiResponse.success(SuccessType.TRANSFER_TARGET_LIST_LOADED, data);
    }
}
