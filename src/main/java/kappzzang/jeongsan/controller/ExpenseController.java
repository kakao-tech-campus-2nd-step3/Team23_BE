package kappzzang.jeongsan.controller;

import jakarta.validation.Valid;
import kappzzang.jeongsan.controller.docs.ExpenseControllerInterface;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.service.ExpenseService;
import kappzzang.jeongsan.service.PersonalExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController implements ExpenseControllerInterface {

    private final ExpenseService expenseService;
    private final PersonalExpenseService personalExpenseService;

    @Override
    @GetMapping("{teamId}")
    public ResponseEntity<JeongsanApiResponse<ExpenseResponse>> getAllExpenses(
        @AuthenticationPrincipal Long memberId,
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
            expenseService.getExpenses(memberId, teamId, status, isChecked));
    }

    @PostMapping("/personal/{teamId}/{expenseId}/{memberId}")
    public ResponseEntity<JeongsanApiResponse<Void>> savePersonalExpense(
        @PathVariable("teamId") Long teamId, @PathVariable("expenseId") Long expenseId, @PathVariable("memberId") Long memberId,
        @Valid @RequestBody SavePersonalExpenseRequest personalExpense) {
        personalExpenseService.savePersonalExpense(memberId, teamId, expenseId, personalExpense);
        return JeongsanApiResponse.success(SuccessType.PERSONAL_EXPENSE_SAVED);
    }
}
