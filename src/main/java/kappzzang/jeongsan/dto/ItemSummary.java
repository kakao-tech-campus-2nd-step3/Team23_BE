package kappzzang.jeongsan.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kappzzang.jeongsan.domain.Item;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ItemSummary(@NotBlank @Size(max = 20) String name,
                          @NotNull @PositiveOrZero @Max(value = 1000000) Integer quantity,
                          @NotNull @PositiveOrZero Integer unitPrice) {

    public Item toEntity() {
        return Item.builder()
            .name(name.trim())
            .unitPrice(unitPrice)
            .quantity(quantity)
            .build();
    }
}
