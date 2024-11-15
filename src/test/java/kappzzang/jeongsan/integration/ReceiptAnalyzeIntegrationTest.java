package kappzzang.jeongsan.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.ItemSummary;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.global.client.clova.ClovaOcrProperties;
import kappzzang.jeongsan.global.client.clova.ClovaOcrProperties.GeneralOcr;
import kappzzang.jeongsan.global.client.dto.response.ChatGptResponse;
import kappzzang.jeongsan.global.client.dto.response.GeneralOcrResponse;
import kappzzang.jeongsan.global.client.openai.OpenAiProperties;
import kappzzang.jeongsan.global.common.dto.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.global.common.util.JwtUtil;
import kappzzang.jeongsan.repository.TestDataUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReceiptAnalyzeIntegrationTest {

    private static final String TEST_RECEIPT_TITLE = "김밥천국";
    private static final String TEST_ITEM_NAME_A = "참치김밥";
    private static final String TEST_ITEM_NAME_B = "라면";
    private static final Integer TEST_ITEM_QUANTITY = 1;
    private static final Integer TEST_ITEM_PRICE = 1;
    private static final String TEST_PAYMENT_TIME = "2024-02-15 15:30:00";
    private final Image image = new Image("jpg", null, "TEST_DATA", "TEST_IMAGE_NAME");

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClovaOcrProperties clovaOcrProperties;

    @Autowired
    private OpenAiProperties openAiProperties;

    @PersistenceContext
    private EntityManager entityManager;

    private MockWebServer mockWebServer;

    private GeneralOcrResponse ocrResponse;
    private ChatGptResponse gptResponse;
    private String baseUrl;
    private String token;

    @BeforeEach
    void setUp() throws IOException {
        TestDataUtil testDataUtil = new TestDataUtil(entityManager);

        Member member = testDataUtil.createAndPersistMember("MemberA", new KakaoPayInfo());
        Team team = testDataUtil.createAndPersistTeam();
        team.addMember(member, true, true);

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("").toString();

        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.defaultParser = Parser.JSON;

        token = jwtUtil.createAccessToken(member.getId());

        ocrResponse = getGeneralOcrResponse();
        gptResponse = getChatGptResponse();
    }

    @DisplayName("영수증 분석 요청할 때, 유효한 이미지로 요청한다면, 분석 결과를 반환 받는다")
    @Test
    void receiptAnalysis_whenValidInput_ResponseParsedResult() throws JsonProcessingException {

        //given
        given(clovaOcrProperties.general().url()).willReturn(baseUrl);
        given(openAiProperties.url()).willReturn(baseUrl);

        mockWebServer.enqueue(createJsonResponse(ocrResponse, 200));
        mockWebServer.enqueue(createJsonResponse(gptResponse, 200));

        //when
        JeongsanApiResponse<ParsedReceiptResponse> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(image)
            .when()
            .post("/receipts/analyze")
            .then()
            .statusCode(SuccessType.RECEIPT_ANALYSIS_SUCCESS.getHttpStatusCode().value())
            .extract()
            .as(new TypeRef<>() {
            });

        //then
        ParsedReceiptResponse expected = new ParsedReceiptResponse(TEST_RECEIPT_TITLE,
            TEST_PAYMENT_TIME,
            List.of(
                new ItemSummary(TEST_ITEM_NAME_A, TEST_ITEM_QUANTITY, TEST_ITEM_PRICE),
                new ItemSummary(TEST_ITEM_NAME_B, TEST_ITEM_QUANTITY, TEST_ITEM_PRICE)
            ));

        assertThat(response.getMessage()).isEqualTo(
            SuccessType.RECEIPT_ANALYSIS_SUCCESS.getMessage());
        assertThat(response.getData()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "영수증 분석을 요청할 때, {2}가 발생한다면,에러 응답를 반환 받는다")
    @CsvSource({
        "200, 500, OCR API 에러",
        "500, 200, GPT API 에러"
    })
    void receiptAnalysis_whenExternalApiFails_thenReturnError(int ocrStatus, int gptStatus,
        String description)
        throws JsonProcessingException {

        //given
        given(clovaOcrProperties.general().url()).willReturn(baseUrl);

        mockWebServer.enqueue(createJsonResponse(ocrResponse, ocrStatus));
        mockWebServer.enqueue(createJsonResponse(gptResponse, gptStatus));

        //when //then
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(image)
            .when()
            .post("/receipts/analyze")
            .then()
            .statusCode(ErrorType.RECEIPT_EXTRACTION_FAILED.getHttpStatusCode().value())
            .assertThat()
            .body("status", equalTo("failure"))
            .body("errorCode", equalTo(ErrorType.RECEIPT_EXTRACTION_FAILED.getErrorCode()))
            .body("data", equalTo(null));
    }

    private MockResponse createJsonResponse(Object body, int status)
        throws JsonProcessingException {
        return new MockResponse()
            .setResponseCode(status)
            .setBody(objectMapper.writeValueAsString(body))
            .addHeader("Content-Type", "application/json");
    }

    private GeneralOcrResponse getGeneralOcrResponse() {
        return new GeneralOcrResponse(
            "V2",
            "c7f11ece-432f-4dda-bf86-630345845eb6",
            List.of(new GeneralOcrResponse.ImageResult(
                "SUCCESS",
                "SUCCESS",
                List.of(
                    new GeneralOcrResponse.ImageResult.Field("김밥천국", 0.9999f, true),
                    new GeneralOcrResponse.ImageResult.Field("2024-02-15 15:30:00", 0.9998f, true),
                    new GeneralOcrResponse.ImageResult.Field("참치김밥", 0.9995f, false),
                    new GeneralOcrResponse.ImageResult.Field("4500", 0.9990f, false),
                    new GeneralOcrResponse.ImageResult.Field("2", 0.9999f, false),
                    new GeneralOcrResponse.ImageResult.Field("9000", 0.9995f, true),
                    new GeneralOcrResponse.ImageResult.Field("라면", 0.9997f, false),
                    new GeneralOcrResponse.ImageResult.Field("4000", 0.9993f, false),
                    new GeneralOcrResponse.ImageResult.Field("1", 0.9999f, false),
                    new GeneralOcrResponse.ImageResult.Field("4000", 0.9994f, true)
                )
            ))
        );
    }

    private ChatGptResponse getChatGptResponse() throws JsonProcessingException {
        return new ChatGptResponse(
            List.of(new ChatGptResponse.Choice(
                0,
                new ChatGptResponse.ResponseMessage(
                    "assistant",
                    objectMapper.writeValueAsString(new ParsedReceiptResponse(
                        TEST_RECEIPT_TITLE,
                        TEST_PAYMENT_TIME,
                        List.of(
                            new ItemSummary(
                                TEST_ITEM_NAME_A,
                                TEST_ITEM_QUANTITY,
                                TEST_ITEM_PRICE
                            ),
                            new ItemSummary(
                                TEST_ITEM_NAME_B,
                                TEST_ITEM_QUANTITY,
                                TEST_ITEM_PRICE
                            )
                        )
                    ))
                )
            )),
            new ChatGptResponse.Usage(150, 100, 250)
        );
    }

    @AfterEach
    void terminate() throws IOException {
        mockWebServer.shutdown();
    }

    @TestConfiguration
    static class TestPropertiesConfig {

        @Bean
        @Primary
        public ClovaOcrProperties clovaOcrProperties() {
            ClovaOcrProperties.GeneralOcr mockGeneral = mock(GeneralOcr.class);
            return new ClovaOcrProperties("TEST_KEY", mockGeneral);
        }

        @Bean
        @Primary
        public OpenAiProperties openAiProperties() {
            OpenAiProperties mockProperties = mock(OpenAiProperties.class);
            given(mockProperties.key()).willReturn("TEST_KEY");
            given(mockProperties.model()).willReturn("TEST_MODEL");
            given(mockProperties.authType()).willReturn("TEST_AUTH_TYPE");
            return mockProperties;
        }

    }

}
