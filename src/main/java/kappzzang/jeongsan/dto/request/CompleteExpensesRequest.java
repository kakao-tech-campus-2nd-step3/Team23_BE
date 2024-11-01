package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompleteExpensesRequest(@NotEmpty List<ExpenseId> expenses) {

    public record ExpenseId(@NotNull Long id) {

    }
}
