package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.ItemDetail;
import kappzzang.jeongsan.dto.request.CompleteExpensesRequest;
import kappzzang.jeongsan.dto.request.CompleteExpensesRequest.ExpenseId;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.dto.response.PersonalExpenseDetailResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    private static final Long TEST_EXPENSE_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_TEAM_ID = 1L;
    private static final int TEST_EXPENSES_SIZE = 3;
    private static final int DEFAULT_ITEM_PRICE = 10;
    private static final int DEFAULT_ITEM_QUANTITY = 10;
    private static final String DEFAULT_ITEM_NAME = "DEFAULT_ITEM_NAME";
    private static final String TEST_IMAGE_URL = "TEST_IMAGE_URL";
    private final String TEST_PRE_SIGNED_URL = "TEST_PRE_SIGNED_URL";
    private final String TEST_TITLE = "TEST_TITLE";
    private final Member mockPayer = mock(Member.class);
    private final Team mockTeam = mock(Team.class);
    private List<Long> expenseIds;
    private CompleteExpensesRequest completeExpensesRequest;

    @Mock
    private ImageStorageService mockImageStorageService;

    @Mock
    private ExpenseRepository mockExpenseRepository;

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private PersonalExpenseRepository mockPersonalExpenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
            .title(TEST_TITLE)
            .imageUrl(TEST_IMAGE_URL)
            .items(List.of(new Item("TEST_NAME", 10, 10)))
            .build();
        expenseIds = LongStream.rangeClosed(1, TEST_EXPENSES_SIZE).boxed().toList();
        completeExpensesRequest = new CompleteExpensesRequest(
            expenseIds.stream().map(ExpenseId::new).toList());
    }

    @Test
    @DisplayName("개인 지출 목록 조회 성공 테스트")
    void testGetPersonalExpenseDetailResponseSuccess() {
        //given
        ItemDetail itemDetailA = new ItemDetail(1L, "TEST_ITEM_A", 10, 1000, 10);
        ItemDetail itemDetailB = new ItemDetail(2L, "TEST_ITEM_B", 5, 5000, 0);
        ItemDetail itemDetailC = new ItemDetail(3L, "TEST_ITEM_C", 3, 2000, 5);
        List<ItemDetail> itemDetails = List.of(itemDetailA, itemDetailB, itemDetailC);
        PersonalExpenseDetailResponse expected = new PersonalExpenseDetailResponse(
            expense.getTitle(), TEST_PRE_SIGNED_URL, itemDetails);

        given(mockExpenseRepository.findById(TEST_EXPENSE_ID)).willReturn(
            Optional.ofNullable(expense));
        given(mockExpenseRepository.findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID)).willReturn(
            itemDetails);
        given(mockImageStorageService.getImageUrl(TEST_IMAGE_URL)).willReturn(TEST_PRE_SIGNED_URL);

        //when
        PersonalExpenseDetailResponse actual = expenseService.getPersonalExpenseDetailResponse(
            TEST_EXPENSE_ID,
            TEST_MEMBER_ID);

        //then
        assertThat(actual).isEqualTo(expected);
        then(mockExpenseRepository).should().findById(TEST_EXPENSE_ID);
        then(mockExpenseRepository).should().findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID);
        then(mockImageStorageService).should().getImageUrl(TEST_IMAGE_URL);
    }

    @Test
    @DisplayName("개인 지출 목록 조회 실패(존재하지 않는 지출 ID)")
    void testGetPersonalExpenseDetailResponseFailWithNotFoundExpense() {
        //given
        given(mockExpenseRepository.findById(TEST_EXPENSE_ID)).willThrow(new JeongsanException(
            ErrorType.EXPENSE_NOT_FOUND));

        //when //then
        assertThatThrownBy(
            () -> expenseService.getPersonalExpenseDetailResponse(TEST_EXPENSE_ID, TEST_MEMBER_ID)
        )
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_NOT_FOUND.getMessage());
        then(mockExpenseRepository).should().findById(TEST_EXPENSE_ID);
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

        given(mockExpenseRepository.findByTeamIdAndStatus(teamId, status)).willReturn(expenses);
        given(mockItemRepository.findAllByExpenseId(expense.getId())).willReturn(items);
        given(mockPersonalExpenseRepository.countByMemberIdAndItemIds(memberId,
            items.stream().map(Item::getId).toList())).willReturn(0L);

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList()).isEmpty();
        assertThat(response.totalPrice()).isEqualTo(0);
        assertThat(response.checked()).isTrue();

        then(mockExpenseRepository).should().findByTeamIdAndStatus(teamId, status);
        then(mockItemRepository).should().findAllByExpenseId(expense.getId());
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

        given(mockExpenseRepository.findByTeamIdAndStatus(teamId, status)).willReturn(expenses);
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

        given(expense.getId()).willReturn(1L);
        given(expense.getTitle()).willReturn("Test Expense");
        given(expense.getTotalPrice()).willReturn(1000);
        given(expense.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense.getStatus()).willReturn(Status.COMPLETED);
        given(expense.getCategory()).willReturn(mock(Category.class));

        given(mockExpenseRepository.findByTeamIdAndStatus(teamId, status)).willReturn(expenses);
        given(mockItemRepository.findAllByExpenseId(expense.getId())).willReturn(items);
        given(mockPersonalExpenseRepository.countByMemberIdAndItemIds(memberId,
            items.stream().map(Item::getId).toList())).willReturn(1L);

        // when
        ExpenseResponse response = expenseService.getExpenses(memberId, teamId, status, isChecked);

        // then
        assertThat(response.expenseList()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(expense.getTotalPrice());
        assertThat(response.expenseList().getFirst().title()).isEqualTo(expense.getTitle());
    }


    @DisplayName("지출 완료 처리 성공")
    @Test
    void completeExpenses_Success() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        expenses.forEach(Expense::changeStatusPending);
        given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
        given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);
        //when
        expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID, TEST_MEMBER_ID);

        //then
        assertThat(expenses).extracting(Expense::getStatus).containsOnly(Status.COMPLETED);
        then(mockExpenseRepository).should()
            .findAllByIdWithDetails(expenseIds);
    }


    @DisplayName("지출 완료 처리 실패(이미 완료된 지출 존재)")
    @Test
    void completeExpenses_AlreadyCompleted_Fail() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        expenses.forEach(Expense::changeStatusPending);
        given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
        given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);
        expenses.getFirst().changeStatusComplete(TEST_TEAM_ID, TEST_MEMBER_ID);

        //when //then
        assertThatThrownBy(
            () -> expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ALREADY_COMPLETED.getMessage());
    }

    @DisplayName("지출 완료 처리 실패(존재하지 않는 지출 존재)")
    @Test
    void completeExpenses_NotFoundExpense_Fail() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE - 1);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);

        //when //then
        assertThatThrownBy(
            () -> expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_NOT_FOUND_ID.getMessage());
    }

    @DisplayName("지출 완료 처리 실패(아직 진행중인 지출 존재)")
    @Test
    void completeExpenses_StatusIsOngoing_Fail() {
        //given
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);
        given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
        given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);

        //when //then
        assertThatThrownBy(
            () -> expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ONGOING.getMessage());
    }

    @DisplayName("지출 완료 처리 실패(타 모임의 지출이 포함된 요청)")
    @Test
    void completeExpenses_AnotherTeam_Fail() {
        //given
        final Long INVALID_TEAM_ID = 2L;
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        expenses.forEach(Expense::changeStatusPending);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);
        given(mockTeam.getId()).willReturn(INVALID_TEAM_ID);
        //when //then
        assertThatThrownBy(
            () -> expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_INVALID_TEAM.getMessage());
    }


    @DisplayName("지출 완료 처리 실패(자신이 결제하지 않은 지출이 포함된 요청)")
    @Test
    void completeExpenses_AnotherPayer_Fail() {
        //given
        final Long INVALID_MEMBER_ID = 2L;
        List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
        expenses.forEach(Expense::changeStatusPending);
        given(
            mockExpenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(
            expenses);
        given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
        given(mockPayer.getId()).willReturn(INVALID_MEMBER_ID);

        //when //then
        assertThatThrownBy(
            () -> expenseService.completeExpenses(completeExpensesRequest, TEST_TEAM_ID,
                TEST_MEMBER_ID)).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_INVALID_PAYER.getMessage());
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
