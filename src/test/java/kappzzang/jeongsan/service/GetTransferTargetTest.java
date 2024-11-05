package kappzzang.jeongsan.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import java.util.HashMap;
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
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PersonalExpenseRepository personalExpenseRepository;
    @InjectMocks
    private TeamService teamService;
    Member member1, member2, member3, member4;
    List<PersonalExpense> personalExpenses;
    @BeforeEach
    void setUp() {
        member1 = new Member(1L, "member1", "member1");
        member2 = new Member(2L, "member2", "member2");
        member3 = new Member(3L, "member3", "member3");
        member4 = new Member(4L, "member4", "member4");
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        Item item5 = new Item();
        PersonalExpense personalExpense1 = new PersonalExpense(member1, item1, 1);
        PersonalExpense personalExpense2 = new PersonalExpense(member2, item1, 1);
        PersonalExpense personalExpense3 = new PersonalExpense(member1, item2, 1);
        PersonalExpense personalExpense4 = new PersonalExpense(member1, item3, 1);
        PersonalExpense personalExpense5 = new PersonalExpense(member3, item3, 1);
        PersonalExpense personalExpense6 = new PersonalExpense(member4, item1, 1);
        PersonalExpense personalExpense7 = new PersonalExpense(member2, item4, 1);
        PersonalExpense personalExpense8 = new PersonalExpense(member4, item5, 1);
        personalExpenses = List.of(personalExpense1, personalExpense2, personalExpense3,
            personalExpense4, personalExpense5, personalExpense6, personalExpense7,
            personalExpense8);
    }
    @Test
    @DisplayName("송금 요청 대상과 금액 조회 테스트")
    void getTransferTargetList() {
        // given
        given(teamRepository.findById(anyLong())).willReturn(Optional.of(new Team()));
        given(personalExpenseRepository.findAllByExpenseIdsWithItemAndMember(anyList()))
            .willReturn(personalExpenses);
        ExpenseId id1 = new ExpenseId(1L);
        ExpenseId id2 = new ExpenseId(1L);
        TransferTargetRequest request = new TransferTargetRequest(List.of(id1, id2));
        // when
        List<TransferTargetResponse> response = teamService.getTransferTargetList(1L, request);
        // then
        Map<Long, Integer> expectedTotalPrices = new HashMap<>();
        expectedTotalPrices.put(member1.getId(), 3);
        expectedTotalPrices.put(member2.getId(), 2);
        expectedTotalPrices.put(member3.getId(), 1);
        expectedTotalPrices.put(member4.getId(), 2);
        for (TransferTargetResponse targetResponse : response) {
            Long memberId = targetResponse.memberId();
            Integer actualTotalPrice = targetResponse.amountDue();
            Integer expectedTotalPrice = expectedTotalPrices.get(memberId);
            assertEquals(expectedTotalPrice, actualTotalPrice);
        }
    }
}
