package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Stream;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest.ExpenseId;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonalExpensePriceCalculateTest {

    private static final String MEMBER_A = "MEMBER_A";
    private static final String MEMBER_B = "MEMBER_B";
    private static final String MEMBER_C = "MEMBER_C";

    private static final String ITEM_A = "ITEM_A";
    private static final String ITEM_B = "ITEM_B";
    private static final String ITEM_C = "ITEM_C";

    @Autowired
    private PersonalExpenseRepository personalExpenseRepository;

    @Autowired
    private ExpenseService expenseService;

    @PersistenceContext
    private EntityManager entityManager;

    private TestDataUtil testDataUtil;

    private Expense expense;
    private Long payerId, teamId;
    private List<Member> members;
    private List<Item> items;

    @BeforeEach
    void setUp() {

        testDataUtil = new TestDataUtil(entityManager);

        Team team = testDataUtil.createAndPersistTeam();
        Member memberA = testDataUtil.createAndPersistMember(MEMBER_A, new KakaoPayInfo());
        Member memberB = testDataUtil.createAndPersistMember(MEMBER_B, new KakaoPayInfo());
        Member memberC = testDataUtil.createAndPersistMember(MEMBER_C, new KakaoPayInfo());

        members = List.of(memberA, memberB, memberC);

        Item itemA = createItem(ITEM_A, 1000, 2); //TotalPrice = 2000
        Item itemB = createItem(ITEM_B, 1000, 3); //TotalPrice = 3000
        Item itemC = createItem(ITEM_C, 3000, 3); //TotalPrice = 9000

        items = List.of(itemA, itemB, itemC);

        Category category = testDataUtil.createAndPersistCategory();

        expense = testDataUtil.createAndPersistExpense(team, memberA, category, items);

        payerId = memberA.getId();
        teamId = team.getId();
    }


    @ParameterizedTest(name = "지출의 상태가 진행 중에서 송금 대기로 변경될 때, 해당 지출에 연관된 모든 개인 지출의 총 금액을 산출 및 업데이트한다.")
    @MethodSource("provideSuccessTestScenarios")
    void whenExpenseStateChangeToPending_ThenUpdateAllPersonalExpenseTotalPrice(
        List<Scenario> scenarios) {

        //given
        createPersonalExpensesWithScenario(scenarios);
        List<ExpenseId> expenseIds = List.of(new ExpenseId(expense.getId()));
        ChangeExpensesStateRequest request = new ChangeExpensesStateRequest(Status.PENDING,
            expenseIds);

        //when
        expenseService.updateExpensesState(request, teamId, payerId);

        //then
        List<PersonalExpense> updatedExpenses = personalExpenseRepository.findAllByItemIds(
            expense.getItemIds());

        for (Scenario scenario : scenarios) {
            PersonalExpense actual = updatedExpenses.stream()
                .filter(pe -> pe.getMember().getNickname().equals(scenario.memberName)
                    && pe.getItem().getName().equals(scenario.itemName))
                .findFirst()
                .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_ONGOING));

            assertThat(actual.getTotalPrice()).isEqualTo(scenario.expectedTotalPrice());
        }
    }

    private static Stream<Arguments> provideSuccessTestScenarios() {
        return Stream.of(
            Arguments.of(
                List.of(
                    // ItemA 선택
                    new Scenario(ITEM_A, MEMBER_A, 1, 1000), // MemberA 1개 선택 -> 1000원
                    new Scenario(ITEM_A, MEMBER_B, 1, 1000), // MemberB 1개 선택 -> 1000원

                    // ItemB 선택
                    new Scenario(ITEM_B, MEMBER_A, 1, 1000), // MemberA 1개 선택 -> 1000원
                    new Scenario(ITEM_B, MEMBER_B, 1, 1000), // MemberB 1개 선택 -> 1000원
                    new Scenario(ITEM_B, MEMBER_C, 1, 1000), // MemberC 1개 선택 -> 1000원

                    // ItemC 선택
                    new Scenario(ITEM_C, MEMBER_C, 3, 9000)  // MemberC 3개 선택 -> 9000원
                )
            ),

            Arguments.of(
                List.of(
                    // ItemA 선택
                    new Scenario(ITEM_A, MEMBER_A, 2, 2000), // MemberA 2개 선택 -> 2000원

                    // ItemB 선택
                    new Scenario(ITEM_B, MEMBER_B, 2, 2000), // MemberB 2개 선택 -> 2000원
                    new Scenario(ITEM_B, MEMBER_C, 1, 1000), // MemberC 1개 선택 -> 1000원

                    // ItemC 선택
                    new Scenario(ITEM_C, MEMBER_A, 1, 3000), // MemberA 1개 선택 -> 3000원
                    new Scenario(ITEM_C, MEMBER_C, 2, 6000)  // MemberC 2개 선택 -> 6000원
                )
            ),

            Arguments.of(
                List.of(
                    // ItemA 선택
                    new Scenario(ITEM_A, MEMBER_A, 1, 666),  // MemberA 1개 선택 -> 666원
                    new Scenario(ITEM_A, MEMBER_B, 1, 666),  // MemberB 1개 선택 -> 666원
                    new Scenario(ITEM_A, MEMBER_C, 1, 668),  // MemberC 1개 선택 -> 668원

                    // ItemB 선택
                    new Scenario(ITEM_B, MEMBER_B, 3, 3000), // MemberB 3개 선택 -> 3000원

                    // ItemC 선택
                    new Scenario(ITEM_C, MEMBER_B, 2, 6000), // MemberB 2개 선택 -> 6000원
                    new Scenario(ITEM_C, MEMBER_C, 1, 3000)  // MemberC 1개 선택 -> 3000원
                )
            )
        );
    }

    @ParameterizedTest(name = "지출의 상태가 진행 중에서 송금 대기로 변경될 때, {1}, ExpenseItemNotSelected 예외을 발생시킨다")
    @MethodSource("provideFailTestScenarios")
    void whenExpenseStateChangeToPending_WithNotSelectedItem_ThrowNotSelectedItemException(
        List<Scenario> scenarios, String description) {

        //given
        createPersonalExpensesWithScenario(scenarios);
        List<ExpenseId> expenseIds = List.of(new ExpenseId(expense.getId()));
        ChangeExpensesStateRequest request = new ChangeExpensesStateRequest(Status.PENDING,
            expenseIds);

        //when //then
        assertThatThrownBy(
            () -> expenseService.updateExpensesState(request, teamId, payerId))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ITEM_NOT_SELECTED.getMessage());

    }

    private static Stream<Arguments> provideFailTestScenarios() {
        return Stream.of(
            Arguments.of(
                List.of(
                    // ItemA 선택
                    new Scenario(ITEM_A, MEMBER_A, 1, 1000), // MemberA 1개 선택 -> 1000원
                    new Scenario(ITEM_A, MEMBER_B, 1, 1000), // MemberB 1개 선택 -> 1000원

                    // ItemB 선택
                    new Scenario(ITEM_B, MEMBER_A, 1, 1000), // MemberA 1개 선택 -> 1000원
                    new Scenario(ITEM_B, MEMBER_B, 1, 1000), // MemberB 1개 선택 -> 1000원
                    new Scenario(ITEM_B, MEMBER_C, 1, 1000)  // MemberC 1개 선택 -> 1000원

                    // ItemC 선택: 아무도 선택하지 않음
                ),
                "아무도 선택하지 않은 품목이 존재하는 경우"
            ),

            Arguments.of(
                List.of(
                    // ItemA 선택
                    new Scenario(ITEM_A, MEMBER_A, 2, 2000), // MemberA 2개 선택 -> 2000원

                    // ItemB 선택
                    new Scenario(ITEM_B, MEMBER_B, 2, 2000), // MemberB 2개 선택 -> 2000원
                    new Scenario(ITEM_B, MEMBER_C, 1, 1000), // MemberC 1개 선택 -> 1000원

                    // ItemC 선택
                    new Scenario(ITEM_C, MEMBER_A, 0, 0),    // MemberA 0개 선택 -> 0원
                    new Scenario(ITEM_C, MEMBER_B, 0, 0),    // MemberB 0개 선택 -> 0원
                    new Scenario(ITEM_C, MEMBER_C, 0, 0)     // MemberC 0개 선택 -> 0원
                ),
                "품목에 대한 모든 선택 수량이 0인 경우"
            )
        );
    }

    @DisplayName("지출의 상태가 진행 중에서 송금 대기로 변경될 때, 품목 선택 수량이 품목 수량보다 적을 시, ExpenseItemSelectionInsufficient 예외을 발생시킨다")
    @Test
    void whenExpenseStateChangeToPending_WithInsufficientSelection_ThrowItemSelectionInsufficientException() {

        List<Scenario> scenarios = List.of(
            // ItemA 선택
            new Scenario(ITEM_A, MEMBER_A, 2, 2000), // MemberA 2개 선택 -> 2000원

            // ItemB 선택
            new Scenario(ITEM_B, MEMBER_B, 2, 2000), // MemberB 2개 선택 -> 2000원
            new Scenario(ITEM_B, MEMBER_C, 1, 1000), // MemberC 1개 선택 -> 1000원

            // ItemC 선택
            new Scenario(ITEM_C, MEMBER_A, 1, 0)    // MemberA 1개 선택 -> 0원
        );

        //given
        createPersonalExpensesWithScenario(scenarios);
        List<ExpenseId> expenseIds = List.of(new ExpenseId(expense.getId()));
        ChangeExpensesStateRequest request = new ChangeExpensesStateRequest(Status.PENDING,
            expenseIds);

        //when //then
        assertThatThrownBy(
            () -> expenseService.updateExpensesState(request, teamId, payerId))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ITEM_SELECTION_INSUFFICIENT.getMessage());

    }


    private void createPersonalExpensesWithScenario(List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            Member member = members.stream()
                .filter(m -> m.getNickname().equals(scenario.memberName())).findFirst()
                .orElse(null);
            Item item = items.stream().filter(i -> i.getName().equals(scenario.itemName()))
                .findFirst().orElse(null);
            Integer quantity = scenario.quantity;
            testDataUtil.createAndPersistPersonalExpense(member, quantity, item, 0);
        }
    }

    private Item createItem(String name, Integer unitPrice, Integer quantity) {
        return Item.builder()
            .name(name)
            .unitPrice(unitPrice)
            .quantity(quantity)
            .build();
    }

    record Scenario(String itemName, String memberName, Integer quantity,
                    Integer expectedTotalPrice) {

    }
}
