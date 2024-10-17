package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record SavePersonalExpenseRequest(@NotEmpty List<ItemInfo> items) {

    public record ItemInfo(@NotEmpty Long itemId, @Positive Integer quantity) {

    }

}
