package kappzzang.jeongsan.global.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.response.GeneralOcrResponse;
import kappzzang.jeongsan.global.client.clova.ClovaApiClient;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.client.clova.ClovaOcrProperties;
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

@RestClientTest(ClovaApiClient.class)
@MockBean(JpaMetamodelMappingContext.class)
@EnableRetry
public class ClovaApiClientTest {

    private static final String TEST_URL = "TEST_URL";
    private static final int MAX_ATTEMPTS = 2;
    private final Image testImage = new Image("", "", "", "");

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ClovaApiClient clovaApiClient;

    @MockBean
    private ClovaOcrProperties clovaOcrProperties;

    @BeforeEach
    void setUp() {
        when(clovaOcrProperties.general()).thenReturn(new ClovaOcrProperties.GeneralOcr(TEST_URL));
    }

    @Test
    void ocrApi_5xxResponse_failsAfterRetries() {

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        JeongsanException exception = assertThrows(JeongsanException.class,
            () -> clovaApiClient.requestClovaGeneralOcr(testImage));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_GENERAL_ERROR);

        mockRestServiceServer.verify();
    }

    @Test
    void ocrApi_timeout_failsAfterRetries() {

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            mockRestServiceServer.expect(requestTo(TEST_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out",
                        new SocketTimeoutException());
                });
        }

        JeongsanException exception = assertThrows(JeongsanException.class,
            () -> clovaApiClient.requestClovaGeneralOcr(testImage));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_REQUEST_TIMEOUT);

        mockRestServiceServer.verify();
    }

    @Test
    void ocrApi_successfulResponse_returnsGeneralOcrResponse() {

        String mockResponse = """
            {
                "version": "V2",
                "requestId": "test-request-id",
                "images": [
                    {
                        "inferResult": "SUCCESS",
                        "message": "SUCCESS",
                        "fields": [
                            {
                                "inferText": "제주도 한라봉 김치",
                                "inferConfidence": 0.9999,
                                "lineBreak": false
                            }
                        ]
                    }
                ]
            }
            """;

        mockRestServiceServer.expect(requestTo(TEST_URL))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        GeneralOcrResponse response = clovaApiClient.requestClovaGeneralOcr(testImage);

        assertThat(response).isNotNull();
        assertThat(response.version()).isEqualTo("V2");
        assertThat(response.requestId()).isEqualTo("test-request-id");
        assertThat(response.images()).hasSize(1);

        GeneralOcrResponse.ImageResult imageResult = response.images().get(0);
        assertThat(imageResult.inferResult()).isEqualTo("SUCCESS");
        assertThat(imageResult.message()).isEqualTo("SUCCESS");

        GeneralOcrResponse.ImageResult.Field field = imageResult.fields().get(0);
        assertThat(field.inferText()).isEqualTo("제주도 한라봉 김치");
        assertThat(field.inferConfidence()).isEqualTo(0.9999f);
        assertThat(field.lineBreak()).isFalse();

        mockRestServiceServer.verify();
    }

}
