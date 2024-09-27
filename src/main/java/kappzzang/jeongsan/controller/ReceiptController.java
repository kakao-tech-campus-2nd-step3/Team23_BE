package kappzzang.jeongsan.controller;

import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/analyze")
    public ResponseEntity<JeongsanApiResponse<ParsedReceiptResponse>> analyzeReceipt(@RequestBody
    Image image) {
        ParsedReceiptResponse parsedReceiptResponse = receiptService.extractReceiptData(image);
        return JeongsanApiResponse.success(SuccessType.RECEIPT_ANALYSIS_SUCCESS,
            parsedReceiptResponse);
    }

    @PostMapping("/analyze/test")
    public ResponseEntity<JeongsanApiResponse<Void>> mockAnalyzeReceipt(@RequestBody
    Image image) {
        return JeongsanApiResponse.success(SuccessType.RECEIPT_ANALYSIS_SUCCESS);
    }

}
