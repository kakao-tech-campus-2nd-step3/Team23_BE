package kappzzang.jeongsan.global.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import kappzzang.jeongsan.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.dto.response.KakaoProfileResponse.KakaoAccount;
import kappzzang.jeongsan.dto.response.KakaoProfileResponse.KakaoAccount.Profile;
import kappzzang.jeongsan.global.client.kakao.KakaoApiClient;
import kappzzang.jeongsan.global.client.kakao.KakaoProfileProperties;
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

@RestClientTest(KakaoApiClient.class)
@MockBean(JpaMetamodelMappingContext.class)
@EnableRetry
public class KakaoApiClientTest {

    private static final String TEST_URL = "TEST_URL";
    private static final String TEST_AUTH_TYPE = "TEST_AUTH_TYPE";
    private static final String TEST_KAKAO_TOKEN = "TEST_KAKAO_TOKEN";
    private static final int MAX_ATTEMPTS = 2;

    @MockBean
    private KakaoProfileProperties kakaoProfileProperties;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private KakaoApiClient kakaoApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        when(kakaoProfileProperties.url()).thenReturn(TEST_URL);
        when(kakaoProfileProperties.authType()).thenReturn(TEST_AUTH_TYPE);
    }

    @Test
    void kakaoApi_5xxResponse_failsAfterRetries() {
        // Given
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        // When & Then
        JeongsanException actual = assertThrows(JeongsanException.class,
            () -> kakaoApiClient.getKakaoProfile(TEST_KAKAO_TOKEN));
        assertThat(actual.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_GENERAL_ERROR);
        mockRestServiceServer.verify();
    }

    @Test
    void kakaoApi_timeout_failsAfterRetries() {
        // Given
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out", new SocketTimeoutException());
                });
        }

        // When & Then
        JeongsanException actual = assertThrows(JeongsanException.class,
            () -> kakaoApiClient.getKakaoProfile(TEST_KAKAO_TOKEN));
        assertThat(actual.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_REQUEST_TIMEOUT);
        mockRestServiceServer.verify();
    }

    @Test
    void kakaoApi_successfulResponse_returnChatGptResponse() throws JsonProcessingException {
        // Given
        Long id = 1L;
        Profile profile = new Profile("홍길동", "http://yyy.kakao.com/.../img_110x110.jpg");
        KakaoAccount kakaoAccount = new KakaoAccount("sample@sample.com", profile);
        KakaoProfileResponse expected = new KakaoProfileResponse(id, kakaoAccount);
        String mockResponse = objectMapper.writeValueAsString(expected);
        mockRestServiceServer.expect(requestTo(TEST_URL))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // When
        KakaoProfileResponse actual = kakaoApiClient.getKakaoProfile(TEST_KAKAO_TOKEN);

        // Then
        assertThat(actual).isEqualTo(expected);
        mockRestServiceServer.verify();
    }
}
