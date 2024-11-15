package kappzzang.jeongsan.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.ItemSummary;
import kappzzang.jeongsan.dto.request.SaveExpenseRequest;
import kappzzang.jeongsan.dto.response.SaveExpenseResponse;
import kappzzang.jeongsan.global.client.aws.AwsClient;
import kappzzang.jeongsan.global.common.dto.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.global.common.util.JwtUtil;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SaveExpenseIntegrationTest {

    private static final int TEST_ITEM_QUANTITY_A = 2;
    private static final int TEST_ITEM_QUANTITY_B = 1;
    private static final int TEST_ITEM_QUANTITY_C = 3;
    private static final int TEST_ITEM_PRICE_A = 1000;
    private static final int TEST_ITEM_PRICE_B = 1500;
    private static final int TEST_ITEM_PRICE_C = 3000;
    private static final String TEST_EXPENSE_NAME = "TEST_EXPENSE_NAME";
    private static final String TEST_ITEM_NAME_A = "TEST_ITEM_NAME_A";
    private static final String TEST_ITEM_NAME_B = "TEST_ITEM_NAME_B";
    private static final String TEST_ITEM_NAME_C = "TEST_ITEM_NAME_C";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AwsClient awsClient;

    @Autowired
    private TestDataUtil testDataUtil;

    private Member member;
    private Team team;
    private String token;
    private Long categoryId;

    private final Image image = new Image("jpg", null, "base64", "TEST_IMAGE");

    @Transactional
    @BeforeEach
    @Commit
    void setUp() {
        member = testDataUtil.createAndPersistMember("MemberA", new KakaoPayInfo());
        team = testDataUtil.createAndPersistTeam();
        team.addMember(member, true, true);
        categoryId = testDataUtil.createAndPersistCategory().getId();

        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.defaultParser = Parser.JSON;

        testDataUtil.commit();
        testDataUtil.clear();
    }

    @DisplayName("사용자가 지출을 저장할 때, 입력값이 유효하다면, 지출이 성공적으로 저장된다")
    @Test
    void saveExpense_WithValidItemsAndImage_ShouldSaveSuccessfully() {

        token = jwtUtil.createAccessToken(member.getId());

        memberRepository.findById(member.getId()) //디버깅 용
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));

        //given
        SaveExpenseRequest request = new SaveExpenseRequest(
            TEST_EXPENSE_NAME,
            LocalDateTime.now(),
            categoryId,
            image,
            List.of(
                new ItemSummary(TEST_ITEM_NAME_A, TEST_ITEM_QUANTITY_A, TEST_ITEM_PRICE_A),
                new ItemSummary(TEST_ITEM_NAME_B, TEST_ITEM_QUANTITY_B, TEST_ITEM_PRICE_B),
                new ItemSummary(TEST_ITEM_NAME_C, TEST_ITEM_QUANTITY_C, TEST_ITEM_PRICE_C)
            )
        );
        given(awsClient.uploadImage(any())).willReturn("TEST_URL");

        //when
        JeongsanApiResponse<SaveExpenseResponse> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/receipts/" + team.getId())
            .then()
            .statusCode(SuccessType.EXPENSE_CREATED.getHttpStatusCode().value())
            .extract()
            .as(new TypeRef<>() {
            });

        //then
        Long expenseId = response.getData().expenseId();
        Expense actual = expenseRepository.findExpenseByIdWithItem(expenseId)
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));

        assertThat(actual.getTitle()).isEqualTo(TEST_EXPENSE_NAME);
        assertThat(actual.getPayer().getId()).isEqualTo(member.getId());
        assertThat(actual.getTeam().getId()).isEqualTo(team.getId());

        List<Item> items = actual.getItems();
        assertThat(items).hasSize(3);

        assertThat(items).extracting("name", "quantity", "unitPrice")
            .containsExactlyInAnyOrder(
                tuple(TEST_ITEM_NAME_A, TEST_ITEM_QUANTITY_A, TEST_ITEM_PRICE_A),
                tuple(TEST_ITEM_NAME_B, TEST_ITEM_QUANTITY_B, TEST_ITEM_PRICE_B),
                tuple(TEST_ITEM_NAME_C, TEST_ITEM_QUANTITY_C, TEST_ITEM_PRICE_C)
            );
    }

    @Test
    @DisplayName("사용자가 지출을 저장할 때, 카테고리가 존재하지 않으면 저장에 실패한다")
    void saveExpense_WithNonExistentCategory_ReturnError() {

        //given
        SaveExpenseRequest invalidRequest = new SaveExpenseRequest(
            TEST_EXPENSE_NAME,
            LocalDateTime.now(),
            999L,
            image,
            List.of(
                new ItemSummary(TEST_ITEM_NAME_A, TEST_ITEM_QUANTITY_A, TEST_ITEM_PRICE_A),
                new ItemSummary(TEST_ITEM_NAME_B, TEST_ITEM_QUANTITY_B, TEST_ITEM_PRICE_B),
                new ItemSummary(TEST_ITEM_NAME_C, TEST_ITEM_QUANTITY_C, TEST_ITEM_PRICE_C)
            )
        );

        //when //then
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(invalidRequest)
            .when()
            .post("/receipts/" + team.getId())
            .then()
            .statusCode(ErrorType.CATEGORY_NOT_FOUND.getHttpStatusCode().value());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public AwsClient awsClient() {
            return mock(AwsClient.class);
        }

        @Bean
        public TestDataUtil testDataUtil(EntityManager entityManager) {
            return new TestDataUtil(entityManager);
        }

    }

}
