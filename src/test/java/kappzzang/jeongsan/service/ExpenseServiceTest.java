package kappzzang.jeongsan.service;

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
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest.ExpenseId;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private ChangeExpensesStateRequest completeExpensesRequest;
    private ChangeExpensesStateRequest pendingExpensesRequest;

    @Mock
    private MemberRepository memberRepository;

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

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
            .title(TEST_TITLE)
            .imageUrl(TEST_IMAGE_URL)
            .items(List.of(new Item("TEST_NAME", 10, 10)))
            .build();
        expenseIds = LongStream.rangeClosed(1, TEST_EXPENSES_SIZE).boxed().toList();
        List<ExpenseId> ids = expenseIds.stream().map(ExpenseId::new).toList();
        completeExpensesRequest = new ChangeExpensesStateRequest(Status.COMPLETED, ids);
        pendingExpensesRequest = new ChangeExpensesStateRequest(Status.PENDING, ids);
    }

    @Test
    @DisplayName("지출 상태 내역을 조회할 때, 존재하지 않는 지출에 대해 요청 시, NotFoundException을 발생 시킨다.")
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
    @DisplayName("지출 목록 조회 - 대기 상태")
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
        assertThat(response.expenseList()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(1000);
        assertThat(response.expenseList().getFirst().title()).isEqualTo("Test Expense");
        assertThat(response.expenseList().getFirst().totalPrice()).isEqualTo(1000);

        assertThat(response.expenseList().getFirst().personalExpense()).isEqualTo(3000);
        assertThat(response.totalPersonalExpense()).isEqualTo(3000);
    }


    @Test
    @DisplayName("내가 지불한 지출 내역 - 빈 리스트")
    void getExpensesIPaid_EmptyList() {
        // given
        Member member = new Member();
        Team team = new Team();
        given(expenseRepository.findExpensesIPaid(member, team, Status.PENDING))
            .willReturn(Collections.emptyList());
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(team));
        given(memberRepository.findById(any(Long.class))).willReturn(Optional.of(member));

        // when
        ExpenseResponse response = expenseService.getExpensesIPaid(TEST_MEMBER_ID, TEST_TEAM_ID);

        // then
        assertThat(response.expenseList()).isEmpty();
        assertThat(response.totalPrice()).isEqualTo(0);
        assertThat(response.checked()).isTrue();
        then(expenseRepository).should().findExpensesIPaid(member, team, Status.PENDING);
    }

    @Test
    @DisplayName("내가 지불한 지출 내역 - 성공")
    void getExpensesIPaid_Success() {
        // given
        Long memberId = 1L;
        Long teamId = 1L;
        Expense expense1 = mock(Expense.class);
        Expense expense2 = mock(Expense.class);

        given(mockPayer.getKakaoId()).willReturn("kakao uuid");

        given(expense1.getPayer()).willReturn(mockPayer);
        given(expense2.getPayer()).willReturn(mockPayer);

        given(expense1.getId()).willReturn(1L);
        given(expense1.getTitle()).willReturn("Test Expense1");
        given(expense1.getTotalPrice()).willReturn(1000);
        given(expense1.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense1.getStatus()).willReturn(Status.ONGOING);
        given(expense1.getCategory()).willReturn(mock(Category.class));

        given(expense2.getId()).willReturn(2L);
        given(expense2.getTitle()).willReturn("Test Expense2");
        given(expense2.getTotalPrice()).willReturn(2000);
        given(expense2.getCreatedAt()).willReturn(LocalDateTime.now());
        given(expense2.getStatus()).willReturn(Status.ONGOING);
        given(expense2.getCategory()).willReturn(mock(Category.class));

        List<Expense> expenses = List.of(expense1, expense2);
        given(teamRepository.findById(any(Long.class))).willReturn(Optional.of(mockTeam));
        given(memberRepository.findById(any(Long.class))).willReturn(Optional.of(mockPayer));
        given(expenseRepository.findExpensesIPaid(mockPayer, mockTeam, Status.PENDING))
            .willReturn(expenses);

        // when
        ExpenseResponse response = expenseService.getExpensesIPaid(memberId, teamId);

        // then
        assertThat(response.expenseList()).hasSize(2);
        assertThat(response.totalPrice()).isEqualTo(3000);
        assertThat(response.checked()).isTrue();
        then(expenseRepository).should().findExpensesIPaid(mockPayer, mockTeam, Status.PENDING);
    }

    @DisplayName("지출 상태 변경(송금 대기 -> 완료)")
    @Nested
    class ChangeExpensesStateComplete {

        @DisplayName("지출 상태 변경 성공")
        @Test
        void changeExpensesState_Complete_Success() {
            //given
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
            expenses.forEach(
                e -> e.changeStatus(mockTeam.getId(), mockPayer.getId(), Status.PENDING));
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);
            //when
            expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID, TEST_MEMBER_ID);

            //then
            assertThat(expenses).extracting(Expense::getStatus).containsOnly(Status.COMPLETED);
            then(expenseRepository).should()
                .findAllByIdWithDetails(expenseIds);
        }

        @DisplayName("지출 상태 변경 실패(이미 완료된 지출 존재)")
        @Test
        void completeExpenses_AlreadyCompleted_Fail() {
            //given
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
            expenses.forEach(
                e -> e.changeStatus(mockTeam.getId(), mockPayer.getId(), Status.PENDING));
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);
            expenses.getFirst().changeStatus(TEST_TEAM_ID, TEST_MEMBER_ID, Status.COMPLETED);

            //when //then
            assertThatThrownBy(
                () -> expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
                    TEST_MEMBER_ID)).isInstanceOf(
                    JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_ALREADY_COMPLETED.getMessage());
        }

        @DisplayName("지출 상태 변경 실패(존재하지 않는 지출 존재)")
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

        @DisplayName("지출 상태 변경 실패(아직 진행중인 지출 존재)")
        @Test
        void completeExpenses_StatusIsOngoing_Fail() {
            //given
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(expenseRepository.findAllByIdWithDetails(expenseIds)).willReturn(expenses);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);

            //when //then
            assertThatThrownBy(
                () -> expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
                    TEST_MEMBER_ID)).isInstanceOf(
                    JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_ONGOING.getMessage());
        }

        @DisplayName("지출 상태 변경 실패(타 모임의 지출이 포함된 요청)")
        @Test
        void completeExpenses_AnotherTeam_Fail() {
            //given
            final Long INVALID_TEAM_ID = 2L;
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
            given(mockTeam.getId()).willReturn(INVALID_TEAM_ID);
            expenses.forEach(
                e -> e.changeStatus(mockTeam.getId(), mockPayer.getId(), Status.PENDING));
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);

            //when //then
            assertThatThrownBy(
                () -> expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
                    TEST_MEMBER_ID)).isInstanceOf(
                    JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_INVALID_TEAM.getMessage());
        }


        @DisplayName("지출 상태 변경 실패(자신이 결제하지 않은 지출이 포함된 요청)")
        @Test
        void completeExpenses_AnotherPayer_Fail() {
            //given
            final Long INVALID_MEMBER_ID = 2L;
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockPayer.getId()).willReturn(INVALID_MEMBER_ID);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            expenses.forEach(
                e -> e.changeStatus(mockTeam.getId(), mockPayer.getId(), Status.PENDING));
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(INVALID_MEMBER_ID);

            //when //then
            assertThatThrownBy(
                () -> expenseService.updateExpensesState(completeExpensesRequest, TEST_TEAM_ID,
                    TEST_MEMBER_ID)).isInstanceOf(
                    JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_INVALID_PAYER.getMessage());
        }
    }

    @DisplayName("지출 상태 변경(정산 중 -> 송금 대기)")
    @Nested
    class changeExpensesStatePending {

        @DisplayName("지출 상태 변경 성공")
        @Test
        void changeExpensesState_Pending_Success() {
            //given
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);
            //when
            expenseService.updateExpensesState(pendingExpensesRequest, TEST_TEAM_ID, TEST_MEMBER_ID);

            //then
            assertThat(expenses).extracting(Expense::getStatus).containsOnly(Status.PENDING);
            then(expenseRepository).should()
                .findAllByIdWithDetails(expenseIds);
        }

        @DisplayName("지출 상태 변경 실패(이미 송금대기 상태 지출 존재)")
        @Test
        void completeExpenses_AlreadyCompleted_Fail() {
            //given
            List<Expense> expenses = createExpenses(TEST_EXPENSES_SIZE);
            given(mockTeam.getId()).willReturn(TEST_TEAM_ID);
            given(mockPayer.getId()).willReturn(TEST_MEMBER_ID);
            expenses
                .forEach(e -> e.changeStatus(mockTeam.getId(), mockPayer.getId(), Status.PENDING));
            given(expenseRepository.findAllByIdWithDetails(expenseIds))
                .willReturn(expenses);

            //when //then
            assertThatThrownBy(
                () -> expenseService.updateExpensesState(pendingExpensesRequest, TEST_TEAM_ID,
                    TEST_MEMBER_ID)).isInstanceOf(
                    JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_ALREADY_PENDING.getMessage());
        }

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
