package kappzzang.jeongsan.controller;

import jakarta.validation.Valid;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.request.SaveExpenseRequest;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.dto.response.SaveExpenseResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.ExpenseService;
import kappzzang.jeongsan.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ExpenseService expenseService;

    @PostMapping("/analyze")
    public ResponseEntity<JeongsanApiResponse<ParsedReceiptResponse>> analyzeReceipt(
        @RequestBody @Valid
        Image image) {
        ParsedReceiptResponse parsedReceiptResponse = receiptService.extractReceiptData(image);
        return JeongsanApiResponse.success(SuccessType.RECEIPT_ANALYSIS_SUCCESS,
            parsedReceiptResponse);
    }

    @PostMapping("/analyze/test")
    public ResponseEntity<JeongsanApiResponse<Void>> mockAnalyzeReceipt(@RequestBody @Valid
    Image image) {
        return JeongsanApiResponse.success(SuccessType.RECEIPT_ANALYSIS_SUCCESS);
    }

    @PostMapping("/{teamId}")
    public ResponseEntity<JeongsanApiResponse<SaveExpenseResponse>> addExpense(@RequestBody @Valid
    SaveExpenseRequest request, @PathVariable("teamId") Long teamId,
        @AuthenticationPrincipal Long memberId) {
        Long savedExpenseId = expenseService.saveExpense(request, teamId, memberId);
        return JeongsanApiResponse.success(SuccessType.EXPENSE_CREATED,
            new SaveExpenseResponse(savedExpenseId));
    }

}
