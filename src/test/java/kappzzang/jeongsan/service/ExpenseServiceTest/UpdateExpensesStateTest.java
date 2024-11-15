package kappzzang.jeongsan.service.ExpenseServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest.ExpenseId;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UpdateExpensesStateTest {

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_TEAM_ID = 1L;
    private static final int TEST_EXPENSES_SIZE = 3;
    private static final int DEFAULT_ITEM_PRICE = 10;
    private static final int DEFAULT_ITEM_QUANTITY = 10;
    private static final String DEFAULT_ITEM_NAME = "DEFAULT_ITEM_NAME";
    private static final String TEST_IMAGE_URL = "TEST_IMAGE_URL";
    private final String TEST_TITLE = "TEST_TITLE";
    private final Member mockPayer = mock(Member.class);
    private final Team mockTeam = mock(Team.class);
    private List<Long> expenseIds;
    private ChangeExpensesStateRequest completeExpensesRequest;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseIds = LongStream.rangeClosed(1, TEST_EXPENSES_SIZE).boxed().toList();
        List<ExpenseId> ids = expenseIds.stream().map(ExpenseId::new).toList();
        completeExpensesRequest = new ChangeExpensesStateRequest(Status.COMPLETED, ids);
    }


    @DisplayName("사용자가 지출 상태를 송금대기에서 완료로 변경할 때, 존재하지 않는 지출에 대해 요청할 경우, ExpenseNotFoundException을 발생시킨다")
    @Test
    void completeExpenses_NotFoundExpense_Fail() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE - 1);
        given(expenseRepository.findAllByIdWithDetails(expenseIds))
            .willReturn(expenses);
        //when //then
        assertThatThrownBy(
            () -> expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_NOT_FOUND_ID.getMessage());
    }


    @DisplayName("사용자는 지출 상태를 송금 대기에서 완료로 변경할 수 있다")
    @Test
    void changeStateToComplete_ThenStateIsChanged() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
        given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
        given(expenseRepository.findAllByIdWithDetails(expenseIds))
            .willReturn(expenses);
        expenses.forEach(e -> ReflectionTestUtils.setField(e, "status", Status.PENDING));

        //when
        expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
            TEST_MEMBER_ID);

        //then
        assertThat(expenses).extracting(Expense::getStatus).containsOnly(Status.COMPLETED);
        then(expenseRepository).should()
            .findAllByIdWithDetails(expenseIds);
    }

    private List<Expense> createExpenses(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(o -> Expense.builder()
                .team(mockTeam)
                .member(mockPayer)
                .title(TEST_TITLE)
                .imageUrl(TEST_IMAGE_URL)
                .items(
                    List.of(new Item(DEFAULT_ITEM_NAME, DEFAULT_ITEM_QUANTITY, DEFAULT_ITEM_PRICE)))
                .build())
            .toList();
    }
}
