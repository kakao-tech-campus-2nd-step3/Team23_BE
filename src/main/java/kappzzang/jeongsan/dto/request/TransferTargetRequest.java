package kappzzang.jeongsan.dto.request;

import java.util.List;

public record TransferTargetRequest(List<ExpenseId> expenses) {

    public record ExpenseId(Long id) {

    }
}
