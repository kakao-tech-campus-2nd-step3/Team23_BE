package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kappzzang.jeongsan.global.common.enumeration.Status;

public record ChangeExpensesStateRequest(Status state, @NotEmpty List<ExpenseId> expenses) {

    public record ExpenseId(@NotNull Long id) {

    }
}
