package kappzzang.jeongsan.global.client.kakao;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import kappzzang.jeongsan.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class KakaoApiClient {

    private static final int MAX_ATTEMPTS = 2;
    private static final int BACK_OFF_DELAY = 500;
    private static final String AUTHORIZATION = "Authorization";

    private final RestClient kakaoClient;
    private final KakaoProperties properties;

    public KakaoApiClient(KakaoProperties properties, RestClient.Builder kakaoClientBuilder) {
        this.properties = properties;
        this.kakaoClient = kakaoClientBuilder.build();
    }

    @Retryable(
        retryFor = RestClientException.class,
        maxAttempts = MAX_ATTEMPTS,
        backoff = @Backoff(delay = BACK_OFF_DELAY)
    )
    public KakaoProfileResponse getKakaoProfile(String kakaoToken) {
        try {
            return kakaoClient.get()
                .uri(properties.url())
                .header(AUTHORIZATION, properties.authType() + kakaoToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .body(KakaoProfileResponse.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException();
        }
    }

    private void handleErrorResponse(HttpRequest request, ClientHttpResponse response)
        throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        log.error("Kakao API error: status={}", statusCode);
        if (statusCode.is5xxServerError()) {
            throw new RestClientException("Server error: " + statusCode);
        }
        throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
    }

    @Recover
    public KakaoProfileResponse recoverFromRestClientException(RestClientException e, String kakaoToken) {
        log.error("Failed to connect Kakao Api after retries");
        if (e.getCause() instanceof SocketTimeoutException
            || e.getCause() instanceof ConnectException) {
            throw new JeongsanException(ErrorType.EXTERNAL_API_REQUEST_TIMEOUT);
        }
        throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
    }
}
