package kappzzang.jeongsan.global.client.openai;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import kappzzang.jeongsan.global.client.dto.request.ChatGptRequest;
import kappzzang.jeongsan.global.client.dto.response.ChatGptResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class OpenAiApiClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BACK_OFF_DELAY = 500;
    private static final int MULTIPLIER = 2;

    private final OpenAiProperties properties;
    private final RestClient openAiClient;
    private final GptPromptManager gptPromptManager;

    public OpenAiApiClient(OpenAiProperties properties, RestClient.Builder openAiClientBuilder,
        GptPromptManager gptPromptManager) {
        this.properties = properties;
        this.openAiClient = openAiClientBuilder.build();
        this.gptPromptManager = gptPromptManager;
    }

    @Retryable(
        retryFor = RestClientException.class,
        noRetryFor = JeongsanException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACK_OFF_DELAY, multiplier = MULTIPLIER)
    )
    public ChatGptResponse extractDataUsingGPT(String message) {
        String instruction = gptPromptManager.getInstruction();
        ChatGptRequest body = new ChatGptRequest(properties.model(), instruction + message);
        return openAiClient.post()
            .uri(properties.url())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
            .body(ChatGptResponse.class);
    }

    private void handleErrorResponse(HttpRequest request, ClientHttpResponse response)
        throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        log.error("OpenAi Api error: status={}", statusCode);
        if (statusCode.is5xxServerError()) {
            throw new RestClientException("Server error: " + statusCode);
        }
        throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
    }

    @Recover
    public ChatGptResponse recoverFromRestClientException(RestClientException e, String message) {
        log.error("Failed to connect OpenAi Api after retries");
        if (e.getCause() instanceof SocketTimeoutException
            || e.getCause() instanceof ConnectException) {
            throw new JeongsanException(ErrorType.EXTERNAL_API_REQUEST_TIMEOUT);
        }
        throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
    }

}
