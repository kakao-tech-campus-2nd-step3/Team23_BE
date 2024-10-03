package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kappzzang.jeongsan.dto.request.CloseTeamRequest;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "모임 관리", description = "모임 목록 생성, 조회, 종료 등을 실행하는 API")
public interface TeamControllerInterface {

    @Operation(summary = "모임 목록 조회 API", description = "모임 목록을 조회하는 API")
    @Parameter(name = "isClosed", description = "모임의 현재 상태(진행 중, 종료)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponse.class))),
    })
    ResponseEntity<JeongsanApiResponse<List<TeamResponse>>> getTeams(Boolean isClosed);

    @Operation(summary = "모임 종료 API", description = "선택한 모임의 상태를 \"종료\"로 변경하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "종료를 원하는 모임의 id"),
        @Parameter(name = "request", description = "`isClosed`에 대한 정보. 사용되지 않을 가능성 있음")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "모임을 `종료` 상태로 변경",
        content = @Content),
        @ApiResponse(responseCode = "400", description = "모임이 이미 종료된 상태 (ErrorCode-E400001)"),
        @ApiResponse(responseCode = "404", description = "`teamId`에 해당하는 모임을 찾을 수 없음 (ErrorCode-E404002)")
    })
    ResponseEntity<JeongsanApiResponse<Void>> closeTeam(Long teamId, CloseTeamRequest request);
}
