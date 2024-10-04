package kappzzang.jeongsan.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.ItemSummary;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveExpenseRequest(
    @NotBlank @Size(max = 30) String title,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime paymentTime,
    @NotNull Long categoryId,
    @NotNull Image image,
    @NotNull List<ItemSummary> items) {

}
