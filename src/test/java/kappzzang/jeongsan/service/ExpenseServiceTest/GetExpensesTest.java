package kappzzang.jeongsan.service.ExpenseServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import kappzzang.jeongsan.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetExpensesTest {

    private static final Long TEST_EXPENSE_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;
    private final Member mockPayer = mock(Member.class);
    private final Team mockTeam = mock(Team.class);

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PersonalExpenseRepository personalExpenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    @DisplayName("지출 상태 내역을 조회할 때, 존재하지 않는 지출에 대해 요청 시, NotFoundException을 발생 시킨다")
    void getPersonalExpenseDetail_NoSuchExpense_ThrowNotFoundException() {
        //given
        given(expenseRepository.findById(TEST_EXPENSE_ID)).willReturn(Optional.empty());

        //when //then
        assertThatThrownBy(
            () -> expenseService.getPersonalExpenseDetailResponse(TEST_EXPENSE_ID, TEST_MEMBER_ID)
        )
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_NOT_FOUND.getMessage());
        then(expenseRepository).should().findById(TEST_EXPENSE_ID);
    }


    @Test
    @DisplayName("지출 목록 조회 - 진행중 상태 빈 경우")
    void getExpenses_givenNoExpenses_Ongoing_ReturnEmptyResponse() {
        // given
        Long memberId = 1L;
        Long teamId = 1L;
        Status status = Status.ONGOING;
        Boolean isChecked = true;
        Expense expense = mock(Expense.class);
        Item item = mock(Item.class);
        List<Expense> expenses = Collections.singletonList(expense);
        List<Item> items = Collections.singletonList(item);

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(mockTeam));

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(itemRepository.findAllByExpenseId(expense.getId())).willReturn(items);
        given(personalExpenseRepository.countByMemberIdAndItemIds(memberId,
            items.stream().map(Item::getId).toList())).willReturn(0L);

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList()).isEmpty();
        assertThat(response.totalPrice()).isEqualTo(0);
        assertThat(response.checked()).isTrue();

        then(expenseRepository).should().findByTeamAndStatus(mockTeam, status);
        then(itemRepository).should().findAllByExpenseId(expense.getId());
    }

    @Test
    @DisplayName("지출 목록 조회 - 완료 상태")
    void getExpenses_Completed() {
        // given
        Long memberId = 1L;
        Long teamId = 1L;
        Status status = Status.COMPLETED;
        Boolean isChecked = null;
        Expense expense = mock(Expense.class);
        List<Expense> expenses = Collections.singletonList(expense);

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(mockTeam));

        given(mockPayer.getKakaoId()).willReturn("kakao uuid");

        given(expense.getPayer()).willReturn(mockPayer);

        given(expense.getId()).willReturn(1L);
        given(expense.getTitle()).willReturn("Test Expense");
        given(expense.getTotalPrice()).willReturn(1000);
        given(expense.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense.getStatus()).willReturn(Status.COMPLETED);
        given(expense.getCategory()).willReturn(mock(Category.class));

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(1000);
        assertThat(response.expenseList().getFirst().title()).isEqualTo("Test Expense");
        assertThat(response.expenseList().getFirst().totalPrice()).isEqualTo(1000);
    }

    @Test
    @DisplayName("지출 목록 반환 - 진행중 상태")
    void getExpenses_Ongoing() {
        // given
        Long memberId = 1L;
        Long teamId = 1L;
        Status status = Status.ONGOING;
        Boolean isChecked = true;
        Expense expense = mock(Expense.class);
        Item item = mock(Item.class);
        List<Expense> expenses = Collections.singletonList(expense);
        List<Item> items = Collections.singletonList(item);

        given(mockPayer.getKakaoId()).willReturn("kakao uuid");

        given(expense.getPayer()).willReturn(mockPayer);

        given(expense.getId()).willReturn(1L);
        given(expense.getTitle()).willReturn("Test Expense");
        given(expense.getTotalPrice()).willReturn(1000);
        given(expense.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense.getStatus()).willReturn(Status.COMPLETED);
        given(expense.getCategory()).willReturn(mock(Category.class));

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(mockTeam));

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(itemRepository.findAllByExpenseId(expense.getId())).willReturn(items);
        given(personalExpenseRepository.countByMemberIdAndItemIds(memberId,
            items.stream().map(Item::getId).toList())).willReturn(1L);

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(expense.getTotalPrice());
        assertThat(response.expenseList().getFirst().title()).isEqualTo(expense.getTitle());
    }

    @Test
    @DisplayName("대기상태인 지출 목록을 조회할 때,"
        + " 사용자의 모임 단위, 모임지출 단위의 총 소비금액을 포함하여 반환한다")
    void getExpenses_Pending() {
        // given
        Long memberId = 1L;
        Long teamId = 1L;
        Status status = Status.PENDING;
        Boolean isChecked = null;
        Expense expense = mock(Expense.class);
        List<Expense> expenses = Collections.singletonList(expense);

        given(expenseRepository.findByTeamAndStatus(mockTeam, status)).willReturn(expenses);
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(mockTeam));
        given(personalExpenseRepository.findPersonalExpenseSum(anyLong(),
            anyLong())).willReturn(3000);

        given(mockPayer.getKakaoId()).willReturn("kakao uuid");

        given(expense.getPayer()).willReturn(mockPayer);
        given(expense.getId()).willReturn(1L);
        given(expense.getTitle()).willReturn("Test Expense");
        given(expense.getTotalPrice()).willReturn(1000);
        given(expense.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense.getStatus()).willReturn(Status.PENDING);
        given(expense.getCategory()).willReturn(mock(Category.class));

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList().getFirst().personalExpense()).isEqualTo(3000);
        assertThat(response.totalPersonalExpense()).isEqualTo(3000);
    }

}
