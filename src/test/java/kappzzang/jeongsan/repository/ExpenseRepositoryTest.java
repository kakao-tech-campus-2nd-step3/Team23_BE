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
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    private List<Expense> expenses;
    private List<Member> members;
    private List<Long> expenseIds;
    private Long payerId;
    private Team team;

    @BeforeEach
    void setUp() {
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo();

        Member memberA = testDataUtil.createAndPersistMember("TEST_USER_A", kakaoPayInfo);
        Member memberB = testDataUtil.createAndPersistMember("TEST_USER_B", kakaoPayInfo);
        Member memberC = testDataUtil.createAndPersistMember("TEST_USER_C", kakaoPayInfo);
        Member payer = testDataUtil.createAndPersistMember("TEST_PAYER", kakaoPayInfo);
        members = List.of(memberA, memberB, memberC);

        team = testDataUtil.createAndPersistTeam();

        Item itemA = testDataUtil.createAndPersistItem("TEST_ITEM_A", 10, 2000);
        Item itemB = testDataUtil.createAndPersistItem("TEST_ITEM_B", 5, 3000);
        Item itemC = testDataUtil.createAndPersistItem("TEST_ITEM_C", 15, 4000);
        Item itemD = testDataUtil.createAndPersistItem("TEST_ITEM_D", 3, 1000);
        Item itemE = testDataUtil.createAndPersistItem("TEST_ITEM_E", 1, 1000);
        Item itemF = testDataUtil.createAndPersistItem("TEST_ITEM_F", 1, 1000);
        Item itemG = testDataUtil.createAndPersistItem("TEST_ITEM_G", 1, 1000);

        PersonalExpense personalExpenseA = testDataUtil.createAndPersistPersonalExpense(memberA, 5,
            itemA, 0);

        PersonalExpense personalExpenseB = testDataUtil.createAndPersistPersonalExpense(memberB, 3,
            itemB, 0);
        PersonalExpense personalExpenseC = testDataUtil.createAndPersistPersonalExpense(memberB, 9,
            itemC, 0);
        PersonalExpense personalExpenseD = testDataUtil.createAndPersistPersonalExpense(memberB,
            10, itemD, 0);

        List<Item> items = List.of(itemA, itemB, itemC, itemD);
        Category category = testDataUtil.createAndPersistCategory();

        expense = testDataUtil.createAndPersistExpense(team, memberA, category, items);

        Expense expense1 = testDataUtil.createAndPersistExpense(team, payer, category,
            List.of(itemE));
        Expense expense2 = testDataUtil.createAndPersistExpense(team, payer, category,
            List.of(itemF));
        Expense expense3 = testDataUtil.createAndPersistExpense(team, payer, category,
            List.of(itemG));
        expenses = List.of(expense1, expense2, expense3);
        payerId = payer.getId();
        expenseIds = List.of(expense1.getId(), expense2.getId(), expense3.getId());
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


    @DisplayName("지출 Id 리스트로 지출과 연관정보를 함께 조회할 수 있다")
    @Test
    void findAllByIdWithDetails_ValidIds_ReturnsExpenses() {
        //when
        List<Expense> actual = expenseRepository.findAllByIdWithDetails(expenseIds);

        //then
        assertThat(actual).hasSize(expenses.size())
            .allMatch(e -> e.getPayer().getId().equals(payerId));
    }

    @DisplayName("존재하지 않는 지출 Id로 조회시 빈 리스트를 반환한다")
    @Test
    void findAllByIdWithDetails_NonExistentIds_ShouldReturnEmptyList() {
        //given
        List<Long> invalidIds = List.of(100L, 200L, 300L);

        //when
        List<Expense> actual = expenseRepository.findAllByIdWithDetails(invalidIds);

        //then
        assertThat(actual).hasSize(0);
    }

    @DisplayName("지출 ID로 아이템 목록이 포함된 지출을 조회한다")
    @Test
    void findExpenseByIdWithItem_ValidId_ReturnsExpense() {
        //when
        Expense actual = expenseRepository.findExpenseByIdWithItem(expense.getId())
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));

        //then
        assertThat(actual.getId()).isEqualTo(expense.getId());
        assertThat(actual.getItems()).hasSize(TEST_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("특정 모임과 상태로 지출 목록을 조회했을 때, 해당하는 지출 조회")
    void testFindByTeamAndStatus() {
        // Given
        Status status = Status.ONGOING;

        // When
        List<Expense> results = expenseRepository.findByTeamAndStatus(team, status);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(
            expense -> expense.getTeam().equals(team) && expense.getStatus().equals(status));
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

