package kappzzang.jeongsan.controller;

import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("{teamId}")
    public ResponseEntity<JeongsanApiResponse<ExpenseResponse>> getAllExpenses(
        @PathVariable Long teamId,
        @RequestParam String state,
        @RequestParam(required = false) Boolean isChecked) {
        Status status;
        try {
            status = Status.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JeongsanException(ErrorType.EXPENSE_INVALID_STATE);
        }

        if (status == Status.ONGOING && isChecked == null) {
            throw new JeongsanException(ErrorType.EXPENSE_MISSING_PARAM);
        }

        return JeongsanApiResponse.success(SuccessType.EXPENSE_LIST_LOADED,
            expenseService.getResponses(1L, teamId, status, isChecked));
    }
}
