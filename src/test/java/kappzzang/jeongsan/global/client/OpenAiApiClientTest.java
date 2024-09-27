package kappzzang.jeongsan.global.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.util.List;
import kappzzang.jeongsan.dto.response.ChatGptResponse;
import kappzzang.jeongsan.dto.response.ChatGptResponse.Choice;
import kappzzang.jeongsan.dto.response.ChatGptResponse.ResponseMessage;
import kappzzang.jeongsan.global.client.openai.GptPromptManager;
import kappzzang.jeongsan.global.client.openai.OpenAiApiClient;
import kappzzang.jeongsan.global.client.openai.OpenAiProperties;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

@RestClientTest(OpenAiApiClient.class)
@MockBean(JpaMetamodelMappingContext.class)
@EnableRetry
public class OpenAiApiClientTest {

    private static final String TEST_URL = "TEST_URL";
    private static final String TEST_REQUEST = "TEST_REQUEST";
    private static final String TEST_INSTRUCTION = "TEST_INSTRUCTION";
    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private OpenAiApiClient openAiApiClient;

    @MockBean
    private OpenAiProperties openAiProperties;

    @MockBean
    private GptPromptManager gptPromptManager;

    @BeforeEach
    void setUp() {
        when(openAiProperties.url()).thenReturn(TEST_URL);
        when(gptPromptManager.getInstruction()).thenReturn(TEST_INSTRUCTION);
    }

    @Test
    void openApi_5xxResponse_failsAfterRetries() {
        // Given
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        // When & Then
        JeongsanException exception = assertThrows(JeongsanException.class,
            () -> openAiApiClient.extractDataUsingGPT(TEST_REQUEST));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_GENERAL_ERROR);
        mockRestServiceServer.verify();
    }

    @Test
    void openApi_timeout_failsAfterRetries() {
        // Given
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out",
                        new SocketTimeoutException());
                });
        }

        // When & Then
        JeongsanException exception = assertThrows(JeongsanException.class,
            () -> openAiApiClient.extractDataUsingGPT(TEST_REQUEST));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_REQUEST_TIMEOUT);
        mockRestServiceServer.verify();
    }

    @Test
    void openApi_successfulResponse_returnChatGptResponse() {
        // Given
        String mockResponse = """
            {
              "choices": [
                {
                  "finish_reason": "stop",
                  "index": 0,
                  "message": {
                    "content": "The 2020 World Series was played in Texas at Globe Life Field in Arlington.",
                    "role": "assistant"
                  },
                  "logprobs": null
                }
              ],
              "created": 1677664795,
              "id": "chatcmpl-7QyqpwdfhqwajicIEznoc6Q47XAyW",
              "model": "gpt-4o-mini",
              "object": "chat.completion",
              "usage": {
                "completion_tokens": 17,
                "prompt_tokens": 57,
                "total_tokens": 74,
                "completion_tokens_details": {
                  "reasoning_tokens": 0
                }
              }
            }
            """;

        mockRestServiceServer.expect(requestTo(TEST_URL))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // When
        ChatGptResponse response = openAiApiClient.extractDataUsingGPT(TEST_REQUEST);

        // Then
        List<Choice> choices = response.choices();
        assertThat(choices).isNotNull();

        Choice choice = choices.get(0);
        assertThat(choice).isNotNull();
        assertThat(choice.index()).isEqualTo(0);

        ResponseMessage message = choice.message();
        assertThat(message).isNotNull();
        assertThat(message.role()).isEqualTo("assistant");
        assertThat(message.content()).isEqualTo(
            "The 2020 World Series was played in Texas at Globe Life Field in Arlington.");

        mockRestServiceServer.verify();
    }
}
