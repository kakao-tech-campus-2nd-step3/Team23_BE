package kappzzang.jeongsan.global.client.dto.request;

import java.util.List;

public record ChatGptRequest(String model, List<RequestMessage> messages) {

    private static final String DEFAULT_ROLE = "user";

    public ChatGptRequest(String model, String prompt) {
        this(model, List.of(new RequestMessage(DEFAULT_ROLE, prompt)));
    }

    public record RequestMessage(String role, String content) {

    }
}
