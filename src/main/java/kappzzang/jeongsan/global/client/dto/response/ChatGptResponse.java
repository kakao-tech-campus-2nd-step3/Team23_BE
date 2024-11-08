package kappzzang.jeongsan.global.client.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

public record ChatGptResponse(List<Choice> choices, Usage usage) {

    public record Choice(Integer index, ResponseMessage message) {

    }

    public record ResponseMessage(String role, String content) {

    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Usage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
    ) {

    }
}
