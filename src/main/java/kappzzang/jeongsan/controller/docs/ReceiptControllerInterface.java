package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.request.SaveExpenseRequest;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.dto.response.SaveExpenseResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "영수증 관리", description = "영수증 분석, 등록, 조회 등을 관리하는 API")
public interface ReceiptControllerInterface {

    @Operation(summary = "영수증 내역 분석&조회 API", description = "영수증 분석 요청을 처리하는 API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영수증 분석 결과 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParsedReceiptResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값 (ErrorCode-E400)", content = @Content),
        @ApiResponse(responseCode = "408", description = "요청 시간 초과 (ErrorCode-E408001)", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류 (ErrorCode-E500001), 영수증 데이터 추출 실패 (ErrorCode-E500002), 외부 API 호출 실패 (ErrorCode-E500003)",
            content = @Content),
    })
    ResponseEntity<JeongsanApiResponse<ParsedReceiptResponse>> analyzeReceipt(Image image);

    @Operation(summary = "영수증 내역 분석&조회 API 테스트", description = "영수증 분석 요청 테스트용 API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영수증 분석 결과 조회 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값 (ErrorCode-E400)", content = @Content),
    })
    ResponseEntity<JeongsanApiResponse<Void>> mockAnalyzeReceipt(Image image);

    @Operation(summary = "지출 내역 저장", description = "영수증 수기 입력 또는 분석 내역 조회 후 수정 값을 저장하는 API")
    @Parameter(name = "teamId", description = "해당 지출이 저장될 teamId")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "지출 내역 저장 완료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaveExpenseResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값 (ErrorCode-E400)", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 (ErrorCode-E404001), 존재하지 않는 모임 (ErrorCode-E404002), 존재하지 않는 카테고리 (ErrorCode-E404)", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류 (ErrorCode-E500001), 외부 API 호출 실패 (ErrorCode-E500003)", content = @Content)
    })
    ResponseEntity<JeongsanApiResponse<SaveExpenseResponse>> addExpense(
        SaveExpenseRequest request, Long teamId, Long memberId);
}
