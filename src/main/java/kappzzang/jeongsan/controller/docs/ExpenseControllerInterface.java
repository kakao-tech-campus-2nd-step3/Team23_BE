package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.response.CategoryListResponse;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.ApiErrorTypeExample;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import org.springframework.http.ResponseEntity;

@Tag(name = "지출 목록 관리", description = "지출 내역에 대한 조회, 저장 등 관리하는 API")
public interface ExpenseControllerInterface {

    @Operation(summary = "지출 내역 목록 조회 API", description = "지출 내역 목록을 쿼리 파라미터의 조건에 따라 조회하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "지출 내역 조회를 원하는 모임의 ID"),
        @Parameter(name = "state", description = "지출 내역의 상태를 지정하는 쿼리 파라미터(`ongoing`, `pending`, `completed`)"),
        @Parameter(name = "isChecked", description = "`정산 중`상태의 지출 목록 중 사용자의 현재 선택한 상태를 지정하는 쿼리 파라미터")
    })
    @ApiResponse(responseCode = "200", description = "지출 내역 목록을 성공적으로 조회", content = @Content(schema = @Schema(implementation = ExpenseResponse.class)))
    @ApiErrorTypeExample({ErrorType.EXPENSE_MISSING_PARAM, ErrorType.EXPENSE_INVALID_STATE,
        ErrorType.TEAM_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<ExpenseResponse>> getAllExpenses(Long memberId, Long teamId,
        String state, Boolean isChecked);

    @Operation(summary = "지출 상태 변경 요청 API", description = "지출의 상태를 변경하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "상태 변경될 지출들의 모임 ID"),
    })
    @ApiResponse(responseCode = "204", description = "지출 상태 변경을 성공")
    @ApiErrorTypeExample({ErrorType.EXPENSE_ALREADY_COMPLETED, ErrorType.EXPENSE_ONGOING,
        ErrorType.EXPENSE_NOT_FOUND_ID, ErrorType.EXPENSE_INVALID_TEAM,
        ErrorType.EXPENSE_INVALID_PAYER, ErrorType.EXPENSE_INVALID_STATE,
        ErrorType.EXPENSE_ALREADY_PENDING, ErrorType.EXPENSE_ITEM_NOT_SELECTED})
    ResponseEntity<JeongsanApiResponse<Void>> changeExpensesStatus(
        ChangeExpensesStateRequest request, Long teamId, Long memberId);

    @Operation(summary = "지출 내역 저장(선택 완료) API", description = "개인이 소비한 품목(아이템)을 선택하여 저장하는 API")
    @Parameters({
        @Parameter(name = "teamId", description = "요청 멤버가 속한 모임의 ID"),
        @Parameter(name = "expenseId", description = "선택한 아이템이 속한 지출의 ID"),
    })
    @ApiResponse(responseCode = "200", description = "개인 소비 내역 저장 성공", content = @Content)
    @ApiErrorTypeExample({ErrorType.INVALID_QUANTITY, ErrorType.NO_CHANGES_NEEDED,
        ErrorType.TEAM_NOT_FOUND, ErrorType.EXPENSE_NOT_FOUND, ErrorType.ITEM_NOT_FOUND,
        ErrorType.TEAM_MEMBER_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<Void>> savePersonalExpense(Long teamId,
        Long expenseId, Long memberId, SavePersonalExpenseRequest personalExpense);

    @Operation(summary = "내가 지불한 지출 내역 조회 API", description = "내가 지불한 지출 내역 중 `송금 대기` 상태 지출 내역 조회")
    @Parameter(name = "teamId", description = "조회를 원하는 모임 ID")
    @ApiResponse(responseCode = "200", description = "지출 내역 목록을 성공적으로 조회")
    @ApiErrorTypeExample(ErrorType.TEAM_NOT_FOUND)
    ResponseEntity<JeongsanApiResponse<ExpenseResponse>> getExpensesIPaid(
        Long memberId, Long teamId);

    @Operation(summary = "카테고리 목록 조회 API", description = "지출 카테고리 목록을 조회하는 API")
    @ApiResponse(responseCode = "200", description = "카테고리 목록을 성공적으로 조회", content = @Content(schema = @Schema(implementation = CategoryListResponse.class)))
    @ApiErrorTypeExample(ErrorType.CATEGORY_NOT_FOUND)
    ResponseEntity<JeongsanApiResponse<CategoryListResponse>> getCategoryList();
}
