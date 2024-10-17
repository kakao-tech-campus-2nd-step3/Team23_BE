package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "지출 목록 관리", description = "지출 내역에 대한 조회, 저장 등 관리하는 API")
public interface ExpenseControllerInterface {

    @Operation(summary = "지출 내역 목록 조회 API", description = "지출 내역 목록을 쿼리 파라미터의 조건에 따라 조회하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "지출 내역 조회를 원하는 모임의 ID"),
        @Parameter(name = "state", description = "지출 내역의 상태를 지정하는 쿼리 파라미터(`ongoing`, `pending`, `completed`)"),
        @Parameter(name = "isChecked", description = "`정산 중`상태의 지출 목록 중 사용자의 현재 선택한 상태를 지정하는 쿼리 파라미터")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "지출 내역 목록을 성공적으로 조회",
            content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
        @ApiResponse(responseCode = "400", description = "누락된 쿼리 파라미터가 존재 (ErrorCode=E400002)"),
        @ApiResponse(responseCode = "400", description = "`state`에 잘못된 값 입력. `ongoing`, `pending`, `completed`만 가능 (ErrorCode-E400003)"),
        @ApiResponse(responseCode = "404", description = "`teamId`에 해당하는 모임이 존재하지 않음. (ErrorCode-E404002)")
    })
    ResponseEntity<JeongsanApiResponse<ExpenseResponse>> getAllExpenses(Long memberId, Long teamId,
        String state, Boolean isChecked);

    @Operation(summary = "지출 내역 저장(선택 완료) API", description = "개인이 소비한 품목(아이템)을 선택하여 저장하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "요청 멤버가 속한 모임의 ID"),
        @Parameter(name = "expenseId", description = "선택한 아이템이 속한 지출의 ID"),
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "개인 소비 내역 저장 성공", content = @Content),
        @ApiResponse(responseCode = "400", description = "아이템 quantity 보다 많은 선택 수량 (ErrorCode=E400)", content = @Content),
        @ApiResponse(responseCode = "400", description = "이전에 선택하여 저장 한 아이템이 포함된 요청 (ErrorCode-E400)", content = @Content),
        @ApiResponse(responseCode = "404", description = "`teamId`, `expenseId`, `itemId`에 해당하는 데이터가 존재하지 않음 (ErrorCode-E404002, 004, 00?)", content = @Content)
    })
    ResponseEntity<JeongsanApiResponse<Void>> savePersonalExpense(Long memberId, Long teamId,
        Long expenseId, SavePersonalExpenseRequest personalExpense);
}
