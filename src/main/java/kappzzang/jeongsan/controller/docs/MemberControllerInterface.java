package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.request.JoinTeamRequest;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "회원 관리", description = "JWT 반환, 멤버 그룹 참여, 초대 현황 등을 관리하는 API Controller")
public interface MemberControllerInterface {

    @Operation(summary = "모임 초대 수락 API", description = "모임 초대 링크를 받은 사용자가 모임 참여를 수락하여 모임에 참여하도록 하는 API")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "모임 초대 수락, 모임 참여 성공"),
        @ApiResponse(responseCode = "400", description = "모임에 초대 되지 않은 멤버. (ErrorCode-E400002"),
        @ApiResponse(responseCode = "400", description = "이미 모임에 참여중인 멤버. (ErrorCode-E400003)"),
        @ApiResponse(responseCode = "404", description = "잘못된 teamId, 모임을 찾을 수 없음. (ErrorCode-E404002)"),
        @ApiResponse(responseCode = "404", description = "잘못된 memberId, 사용자를 찾을 수 없음. (ErrorCode-E404003)")})
    ResponseEntity<JeongsanApiResponse<Void>> joinTeam(Long teamId, JoinTeamRequest request);
}
