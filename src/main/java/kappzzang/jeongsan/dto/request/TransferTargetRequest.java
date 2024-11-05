package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TransferTargetRequest(@NotEmpty List<ExpenseId> expenses) {

    public record ExpenseId(Long id) {

    }
}
