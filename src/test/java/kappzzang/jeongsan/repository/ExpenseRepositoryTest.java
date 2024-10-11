package kappzzang.jeongsan.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.ItemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestDataUtil.class)
public class ExpenseRepositoryTest {

    private static final Integer TEST_ITEM_QUANTITY = 4;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestDataUtil testDataUtil;

    private Expense expense;
    private List<Member> members;

    @BeforeEach
    void setUp() {
        KakaoPayInfo kakaoPayInfo = testDataUtil.createAndPersistKakaoPayInfo();

        Member memberA = testDataUtil.createAndPersistMember("TEST_USER_A", kakaoPayInfo);
        Member memberB = testDataUtil.createAndPersistMember("TEST_USER_B", kakaoPayInfo);
        Member memberC = testDataUtil.createAndPersistMember("TEST_USER_C", kakaoPayInfo);
        members = List.of(memberA, memberB, memberC);

        Team team = testDataUtil.createAndPersistTeam();

        PersonalExpense personalExpenseA = testDataUtil.createAndPersistPersonalExpense(memberA, 5);

        PersonalExpense personalExpenseB = testDataUtil.createAndPersistPersonalExpense(memberB, 3);
        PersonalExpense personalExpenseC = testDataUtil.createAndPersistPersonalExpense(memberB, 9);
        PersonalExpense personalExpenseD = testDataUtil.createAndPersistPersonalExpense(memberB,
            10);

        Item itemA = testDataUtil.createAndPersistItem("TEST_ITEM_A", 10, 2000);
        Item itemB = testDataUtil.createAndPersistItem("TEST_ITEM_B", 5, 3000);
        Item itemC = testDataUtil.createAndPersistItem("TEST_ITEM_C", 15, 4000);
        Item itemD = testDataUtil.createAndPersistItem("TEST_ITEM_D", 3, 1000);

        itemA.addPersonalExpense(personalExpenseA);
        itemB.addPersonalExpense(personalExpenseB);
        itemC.addPersonalExpense(personalExpenseC);
        itemD.addPersonalExpense(personalExpenseD);

        List<Item> items = List.of(itemA, itemB, itemC, itemD);
        Category category = testDataUtil.createAndPersistCategory();

        expense = testDataUtil.createAndPersistExpense(team, memberA, category, items);
    }

    @MethodSource("PersonalExpenseCaseProvider")
    @ParameterizedTest(name = "memberOrder: {0}")
    @DisplayName("맴버 별 지출 상세 조회 테스트(PersonalExpense 미등록 품목 포함)")
    void testFindItemDetailsByExpenseIdAndMemberId(Integer memberOrder, List<?> expectedQuantity) {
        //given
        final String DEFAULT_TEST_FILED = "consumedQuantity";
        Long expenseId = expense.getId();
        Long memberId = members.get(memberOrder).getId();

        //when
        List<ItemDetail> actual = expenseRepository.findItemDetailsByExpenseIdAndMemberId(expenseId,
            memberId);

        //then
        assertThat(actual)
            .isNotNull()
            .hasSize(TEST_ITEM_QUANTITY)
            .satisfies(expected -> {
                for (int i = 0; i < TEST_ITEM_QUANTITY; i++) {
                    assertThat(actual.get(i)).hasFieldOrPropertyWithValue(DEFAULT_TEST_FILED,
                        expectedQuantity.get(i));
                }
            });
    }

    static Stream<Arguments> PersonalExpenseCaseProvider() {
        return Stream.of(
            Arguments.of(0, List.of(5, 0, 0, 0)),
            //0번째 맴버가 선택한 지출(itemA: 5, itemB: 0, itemC: 0, itemD: 0)
            Arguments.of(1, List.of(0, 3, 9, 10)),
            //1번째 맴버가 선택한 지출(itemA: 0, itemB: 3, itemC: 9, itemD: 10)
            Arguments.of(2, List.of(0, 0, 0, 0))
            //2번째 맴버가 선택한 지출(itemA: 0, itemB: 0, itemC: 0, itemD: 0)
        );
    }

}

