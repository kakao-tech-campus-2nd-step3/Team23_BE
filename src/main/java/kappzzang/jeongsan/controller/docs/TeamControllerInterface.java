package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kappzzang.jeongsan.dto.request.CreateTeamRequest;
import kappzzang.jeongsan.dto.request.TransferTargetRequest;
import kappzzang.jeongsan.dto.response.CreateTeamResponse;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.ApiErrorTypeExample;
import kappzzang.jeongsan.dto.response.TransferTargetResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import org.springframework.http.ResponseEntity;

@Tag(name = "모임 관리", description = "모임 생성, 종료, 목록 조회, 멤버 초대 현황 등을 관리하는 API")
public interface TeamControllerInterface {

    @Operation(summary = "모임 목록 조회 API", description = "모임 목록을 조회하는 API")
    @Parameter(name = "isClosed", description = "모임의 현재 상태(진행 중, 종료)")
    @ApiResponse(responseCode = "200", description = "모임 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponse.class)))
    ResponseEntity<JeongsanApiResponse<List<TeamResponse>>> getTeams(Boolean isClosed);

    @Operation(summary = "모임 조회 API", description = "`teamId`를 이용해 모임을 조회하는 API")
    @Parameter(name = "teamId", description = "조회를 원하는 모임의 ID")
    @ApiResponse(responseCode = "200", description = "모임 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponse.class)))
    @ApiErrorTypeExample(ErrorType.TEAM_NOT_FOUND)
    ResponseEntity<JeongsanApiResponse<TeamResponse>> getTeam(Long teamId);

    @Operation(summary = "모임 생성 API", description = "요청한 사용자가 주인으로 모임을 생성하는 API")
    @Parameters({
        @Parameter(name = "name", description = "15글자 이내의 모임 이름. 모임의 owner 기준 동일한 모임 이름을 사용할 수 없음"),
        @Parameter(name = "subject", description = "모임의 목적. 이모지 사용"),
        @Parameter(name = "members", description = "모임에 초대할 사용자들 ID")
    })
    @ApiResponse(responseCode = "201", description = "모임 생성 성공")
    @ApiErrorTypeExample({ErrorType.USER_NOT_FOUND, ErrorType.TEAM_NAME_DUPLICATED})
    ResponseEntity<JeongsanApiResponse<CreateTeamResponse>> createTeam(Long memberId,
        CreateTeamRequest request);

    @Operation(summary = "모임 종료 API", description = "선택한 모임의 상태를 \"종료\"로 변경하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "종료를 원하는 모임의 id")
    })
    @ApiResponse(responseCode = "204", description = "모임을 `종료` 상태로 변경", content = @Content)
    @ApiErrorTypeExample({ErrorType.TEAM_ALREADY_CLOSED, ErrorType.TEAM_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<Void>> closeTeam(Long teamId);

    @Operation(summary = "모임 멤버 초대 현황 조회 API", description = "모임에 초대한 멤버들의 초대 수락/대기 상태를 조회하는 API")
    @Parameter(name = "teamId", description = "멤버 초대 현황을 조회하려는 모임의 id")
    @ApiResponse(responseCode = "200", description = "모임의 멤버 초대 현황 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvitationStatusResponse.class)))
    @ApiErrorTypeExample({ErrorType.TEAM_NOT_FOUND, ErrorType.INVITATION_STATUS_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<List<InvitationStatusResponse>>> getInvitationStatus(
        Long teamId);

    @Operation(summary = "송금 요청 대상 및 금액 조회 API", description = "송금을 요청할 멤버와 해당 멤버가 보내야할 금액을 조회하는 API")
    @Parameter(name = "teamId", description = "송금 요청 대상 및 금액 조회하려는 모임의 id")
    @RequestBody(description = "송금 요청할 지출 id 목록", required = true,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferTargetRequest.class)))
    @ApiResponse(responseCode = "200", description = "송금 요청 대상 및 금액 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TransferTargetResponse.class))))
    @ApiErrorTypeExample({ErrorType.TEAM_NOT_FOUND, ErrorType.PERSONAL_EXPENSE_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<List<TransferTargetResponse>>> getTransferTargetList(
        Long memberId, Long teamId, TransferTargetRequest request);
}
