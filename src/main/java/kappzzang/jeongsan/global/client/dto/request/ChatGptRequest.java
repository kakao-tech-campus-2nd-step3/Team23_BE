package kappzzang.jeongsan.global.client.dto.request;

import java.util.List;

public record ChatGptRequest(String model, List<RequestMessage> messages) {

    private static final String USER = "user";
    private static final String SYSTEM = "system";

    public ChatGptRequest(String model, String prompt, String message) {
        this(model, List.of(new RequestMessage(SYSTEM, prompt), new RequestMessage(USER, message)));
    }

    public record RequestMessage(String role, String content) {

    }
}
