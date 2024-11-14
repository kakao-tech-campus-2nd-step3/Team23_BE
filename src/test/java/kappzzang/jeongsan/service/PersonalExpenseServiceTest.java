package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest.ItemInfo;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalExpenseServiceTest {

    private final Long teamId = 1L;
    private final Long itemId = 1L;
    private final Long expenseId = 1L;
    private final Long memberId = 1L;
    @InjectMocks
    private PersonalExpenseService personalExpenseService;
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
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private Team mockTeam;
    @Mock
    private Member mockMember;
    @Mock
    private Item mockItem;
    @Mock
    private Expense mockExpense;

    @BeforeEach
    void setUp() {
        given(teamRepository.findById(teamId)).willReturn(Optional.of(mockTeam));
        given(itemRepository.findById(itemId)).willReturn(Optional.of(mockItem));
        given(expenseRepository.findById(expenseId)).willReturn(Optional.of(mockExpense));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(mock(Member.class)));
        given(teamMemberRepository.findTeamMemberByTeamAndMember(any(), any())).willReturn(
            Optional.of(mock(TeamMember.class)));
        given(mockExpense.getTeam()).willReturn(mockTeam);
        given(mockItem.getQuantity()).willReturn(5);
    }

    @Test
    @DisplayName("개인 소비 내역을 저장한다.")
    void savePersonaExpense() {
        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new ItemInfo(itemId, 1)));
        given(personalExpenseRepository.findByMemberAndItem(any(), any())).willReturn(
            Optional.empty());

        // when
        personalExpenseService.saveOrUpdatePersonalExpense(memberId, teamId, expenseId, request);

        // then
        then(personalExpenseRepository).should(times(1)).save(any(PersonalExpense.class));
    }

    @Test
    @DisplayName("기존의 개인 소비 수량과 같은 수량의 요청이면 업데이트하지 않는다.")
    void updateWithSameQuantityTest() {
        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new ItemInfo(itemId, 1)));
        PersonalExpense personalExpense = new PersonalExpense(mockMember, mockItem, 1, 1);
        given(personalExpenseRepository.findByMemberAndItem(any(), any())).willReturn(
            Optional.of(personalExpense));

        // when
        personalExpenseService.saveOrUpdatePersonalExpense(memberId, teamId, expenseId, request);

        // then
        then(personalExpenseRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("아이템의 소비 수량보다 큰 수의 수량이 포함된 요청 시 INVALID_QUANTITY 예외가 발생한다")
    void requestWithInvalidQuantityTest() {
        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new ItemInfo(itemId, 10)));

        // when, then
        assertThatThrownBy(() ->
            personalExpenseService.saveOrUpdatePersonalExpense(memberId, teamId, expenseId, request)
        ).isInstanceOf(JeongsanException.class)
            .hasMessageContaining(ErrorType.INVALID_QUANTITY.getMessage());
    }
}
