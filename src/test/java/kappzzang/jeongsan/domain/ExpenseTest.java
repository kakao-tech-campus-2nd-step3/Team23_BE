package kappzzang.jeongsan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseTest {

    private static final Long VALID_TEAM_ID = 1L;
    private static final Long VALID_MEMBER_ID = 1L;
    private static final Long INVALID_TEAM_ID = 2L;
    private static final Long INVALID_MEMBER_ID = 2L;

    @Mock
    private Team team;
    @Mock
    private Member payer;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
            .title("TEST_TITLE")
            .member(payer)
            .team(team)
            .category(new Category())
            .imageUrl("TEST_IMAGE_URL")
            .paymentTime(LocalDateTime.now())
            .items(List.of(new Item("TEST_ITEM", 10, 1000)))
            .build();
    }

    @DisplayName("진행중인 지출을 송금 대기 상태로 변경할 수 있다")
    @Test
    void changeState_OngoingToPending_ShouldSuccess() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        //when
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING);
        //then
        assertThat(expense.getStatus()).isEqualTo(Status.PENDING);
    }

    @DisplayName("송금 대기 중인 지출을 진행중 상태로 변경할 수 있다")
    @Test
    void changeState_PendingToOngoing_ShouldSuccess() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING);
        //when
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.ONGOING);
        //then
        assertThat(expense.getStatus()).isEqualTo(Status.ONGOING);
    }

    @DisplayName("송금 대기 중인 지출을 완료 상태로 변경할 수 있다")
    @Test
    void changeState_PendingToComplete_ShouldSuccess() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING);
        //when
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.COMPLETED);
        //then
        assertThat(expense.getStatus()).isEqualTo(Status.COMPLETED);
    }

    @DisplayName("송금 대기 중인 지출을 다시 송금 대기 상태로 변경하면 예외가 발생한다")
    @Test
    void changeState_PendingToPending_throwAlreadyPendingException() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING);
        //when //then
        assertThatThrownBy(
            () -> expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ALREADY_PENDING.getMessage());
    }

    @DisplayName("진행중인 지출을 완료 상태로 변경하면 예외가 발생한다")
    @Test
    void changeState_OngoingToComplete_throwExpenseOngoingException() {
        //when //then
        assertThatThrownBy(
            () -> expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.COMPLETED))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ONGOING.getMessage());
    }

    @DisplayName("완료된 지출을 다시 완료 상태로 변경하면 예외가 발생한다")
    @Test
    void changeState_CompleteToComplete_throwAlreadyCompleteException() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING);
        expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.COMPLETED);
        //when //then
        assertThatThrownBy(
            () -> expense.changeStatus(VALID_TEAM_ID, VALID_MEMBER_ID, Status.COMPLETED))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_ALREADY_COMPLETED.getMessage());
    }

    @DisplayName("지출의 팀이 일치하지 않으면 상태 변경 시 예외가 발생한다")
    @Test
    void changeState_NotMatchTeam_throwExpenseInvalidTeamException() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        //when //then
        assertThatThrownBy(
            () -> expense.changeStatus(INVALID_TEAM_ID, VALID_MEMBER_ID, Status.PENDING))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_INVALID_TEAM.getMessage());
    }

    @DisplayName("지출의 결제자가 일치하지 않으면 상태 변경 시 예외가 발생한다")
    @Test
    void changeState_NotMatchPayer_throwExpenseInvalidPayerException() {
        //given
        given(team.getId()).willReturn(VALID_TEAM_ID);
        given(payer.getId()).willReturn(VALID_MEMBER_ID);
        //when //then
        assertThatThrownBy(
            () -> expense.changeStatus(VALID_TEAM_ID, INVALID_MEMBER_ID, Status.PENDING))
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_INVALID_PAYER.getMessage());
    }
}
