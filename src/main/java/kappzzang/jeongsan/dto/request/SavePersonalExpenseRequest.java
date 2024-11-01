package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record SavePersonalExpenseRequest(@NotEmpty List<ItemInfo> items) {

    public record ItemInfo(@NotNull Long itemId, @Positive Integer quantity) {

    }
}
