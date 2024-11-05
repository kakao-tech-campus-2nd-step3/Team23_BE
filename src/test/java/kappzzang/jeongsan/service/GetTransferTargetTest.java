package kappzzang.jeongsan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.TransferTargetRequest;
import kappzzang.jeongsan.dto.request.TransferTargetRequest.ExpenseId;
import kappzzang.jeongsan.dto.response.TransferTargetResponse;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetTransferTargetTest {

    Member member1, member2, member3, member4, payer;
    List<PersonalExpense> personalExpenses;
    Map<Long, Integer> expectedTotalPrices;

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PersonalExpenseRepository personalExpenseRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        setUpMembers();
        setUpPersonalExpenses();
        setUpExpectedTotalPrices();
    }

    @Test
    @DisplayName(
        "request에 포함된 지출에 대해 멤버별로 지불해야 할 금액을 합하여 반환한다. "
            + "이때, API 호출자는 결제자이므로 response에 포함하지 않는다.")
    void getTransferTargetList() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.ofNullable(payer));
        given(teamRepository.findById(anyLong())).willReturn(Optional.of(new Team()));
        given(personalExpenseRepository.findAllByExpenseIdsWithItemAndMember(anyList()))
            .willReturn(personalExpenses);

        ExpenseId id1 = new ExpenseId(1L);
        ExpenseId id2 = new ExpenseId(1L);

        TransferTargetRequest request = new TransferTargetRequest(List.of(id1, id2));

        // when
        List<TransferTargetResponse> response =
            teamService.getTransferTargetList(5L, 1L, request);

        // then
        for (TransferTargetResponse targetResponse : response) {
            Long memberId = targetResponse.memberId();
            Integer actualTotalPrice = targetResponse.amountDue();
            Integer expectedTotalPrice = expectedTotalPrices.get(memberId);

            assertNotEquals(payer.getId(), memberId);
            assertEquals(expectedTotalPrice, actualTotalPrice);
        }
    }

    private void setUpMembers() {
        member1 = new Member(1L, "member1", "member1");
        member2 = new Member(2L, "member2", "member2");
        member3 = new Member(3L, "member3", "member3");
        member4 = new Member(4L, "member4", "member4");
        payer = new Member(5L, "payer", "payer");
    }

    private void setUpPersonalExpenses() {
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        Item item5 = new Item();

        personalExpenses = List.of(
            createExpense(member1, item1, 1),
            createExpense(member2, item1, 1),
            createExpense(member1, item2, 1),
            createExpense(member1, item3, 1),
            createExpense(member3, item3, 1),
            createExpense(member4, item1, 1),
            createExpense(member2, item4, 1),
            createExpense(member4, item5, 1),
            createExpense(payer, item1, 1),
            createExpense(payer, item3, 1)
        );
    }

    private void setUpExpectedTotalPrices() {
        expectedTotalPrices = Map.of(
            member1.getId(), 3,
            member2.getId(), 2,
            member3.getId(), 1,
            member4.getId(), 2
        );
    }

    private PersonalExpense createExpense(Member member, Item item, int price) {
        return new PersonalExpense(member, item, price);
    }
}
