package kappzzang.jeongsan.service.ExpenseServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import kappzzang.jeongsan.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetExpenseIPaidTest {

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_TEAM_ID = 1L;
    private final Member mockPayer = mock(Member.class);
    private final Team mockTeam = mock(Team.class);

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

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
}
