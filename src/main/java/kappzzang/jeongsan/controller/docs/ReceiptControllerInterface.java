package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.request.SaveExpenseRequest;
import kappzzang.jeongsan.dto.response.ExpenseDetailResponse;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.dto.response.PersonalExpenseDetailResponse;
import kappzzang.jeongsan.dto.response.SaveExpenseResponse;
import kappzzang.jeongsan.global.common.ApiErrorTypeExample;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import org.springframework.http.ResponseEntity;

@Tag(name = "영수증 관리", description = "영수증 분석, 등록, 조회 등을 관리하는 API")
public interface ReceiptControllerInterface {

    @Operation(summary = "영수증 내역 분석&조회 API", description = "영수증 분석 요청을 처리하는 API")
    @ApiResponse(responseCode = "200", description = "영수증 분석 결과 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParsedReceiptResponse.class)))
    @ApiErrorTypeExample({ErrorType.INVALID_INPUT, ErrorType.EXTERNAL_API_REQUEST_TIMEOUT,
        ErrorType.INTERNAL_SERVER_ERROR})
    ResponseEntity<JeongsanApiResponse<ParsedReceiptResponse>> analyzeReceipt(Image image);

    @Operation(summary = "영수증 내역 분석&조회 API 테스트", description = "영수증 분석 요청 테스트용 API")
    @ApiResponse(responseCode = "200", description = "영수증 분석 결과 조회 성공")
    @ApiErrorTypeExample(ErrorType.INVALID_INPUT)
    ResponseEntity<JeongsanApiResponse<Void>> mockAnalyzeReceipt(Image image);

    @Operation(summary = "지출 내역 저장", description = "영수증 수기 입력 또는 분석 내역 조회 후 수정 값을 저장하는 API")
    @Parameter(name = "teamId", description = "해당 지출이 저장될 teamId")
    @ApiResponse(responseCode = "201", description = "지출 내역 저장 완료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaveExpenseResponse.class)))
    @ApiErrorTypeExample({ErrorType.INVALID_INPUT, ErrorType.USER_NOT_FOUND,
        ErrorType.INTERNAL_SERVER_ERROR})
    ResponseEntity<JeongsanApiResponse<SaveExpenseResponse>> addExpense(
        SaveExpenseRequest request, Long teamId, Long memberId);

    @Operation(summary = "지출 상세 내역 조회", description = "지출 내역 상세를 조회하는 API")
    @Parameter(name = "expenseId", description = "조회할 지출 ID")
    @ApiResponse(responseCode = "200", description = "지출 상세 내역 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PersonalExpenseDetailResponse.class)))
    @ApiErrorTypeExample({ErrorType.EXPENSE_NOT_FOUND, ErrorType.INTERNAL_SERVER_ERROR})
    ResponseEntity<JeongsanApiResponse<PersonalExpenseDetailResponse>> getPersonalExpenseDetails(
        Long memberId, Long expenseId);

    @Operation(summary = "지출 선택 현황 조회 API", description = "지출 선택 현황을 조회하는 API")
    @ApiResponse(responseCode = "200", description = "지출 선택 상세를 성공적으로 조회", content = @Content(schema = @Schema(implementation = ExpenseDetailResponse.class)))
    @ApiErrorTypeExample({ErrorType.EXPENSE_NOT_FOUND, ErrorType.EXPENSE_INVALID_PAYER})
    ResponseEntity<JeongsanApiResponse<ExpenseDetailResponse>> getExpenseDetails(Long expenseId,
        Long memberId);
}
