package kappzzang.jeongsan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest.ItemInfo;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonalExpenseServiceTest {

    @Autowired
    private PersonalExpenseService personalExpenseService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private PersonalExpenseRepository personalExpenseRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Member member1, member2, member3;
    private Team team;
    private Expense expense;
    private Item item1, item2;

    @BeforeAll
    void setup() {
        team = teamRepository.save(new Team("Test Team", "🍎"));
        member1 = memberRepository.save(
            new Member("email1@test.com", "User1", null, null, null));
        member2 = memberRepository.save(
            new Member("email2@test.com", "User2", null, null, null));
        member3 = memberRepository.save(
            new Member("email3@test.com", "User3", null, null, null));
        item1 = new Item("Test Item", 2, 1000);
        item2 = new Item("Test Item", 1, 1000);
        expense = Expense.builder()
            .title("asdf")
            .category(null)
            .imageUrl("image.jpg")
            .team(team)
            .member(member1)
            .items(List.of(item1))
            .paymentTime(LocalDateTime.now())
            .build();
        expense = expenseRepository.save(expense);
        itemRepository.save(item1);
        itemRepository.save(item2);
        personalExpenseRepository.save(new PersonalExpense(member1, item2, 1, 1000));
        teamMemberRepository.save(new TeamMember(member1, team, false, true));
        teamMemberRepository.save(new TeamMember(member2, team, false, true));
        teamMemberRepository.save(new TeamMember(member3, team, false, true));
    }

    @AfterAll
    void cleanup() {
        personalExpenseRepository.deleteAll();
        itemRepository.deleteAll();
        expenseRepository.deleteAll();
        teamRepository.deleteAll();
        memberRepository.deleteAll();
        teamMemberRepository.deleteAll();
    }

    @Test
    @DisplayName("개인 소비 내역 저장 - 동시성 테스트")
    void personalExpenseSaveConcurrencyTest() throws InterruptedException {

        // given
        int threadCount = 3;
        List<Member> members = List.of(member1, member2, member3);
        List<Integer> expectedTotalPrices = List.of(666, 666, 668);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 마지막에 완료된 memberId 저장 목적
        AtomicReference<Long> lastCompletedMemberId = new AtomicReference<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            Member testMember = members.get(i);

            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드 대기
                    SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
                        List.of(new SavePersonalExpenseRequest.ItemInfo(item1.getId(), 1)));
                    personalExpenseService.savePersonalExpense(testMember.getId(), team.getId(),
                        expense.getId(), request);

                    lastCompletedMemberId.set(testMember.getId());  // 마지막에 완료된 memberId 저장
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // then
        List<PersonalExpense> savedExpenses = personalExpenseRepository.findAllByItem(item1);
        List<Integer> actualTotalPrices = savedExpenses.stream().map(PersonalExpense::getTotalPrice)
            .sorted().toList();

        Long lastUpdatedMemberId = lastCompletedMemberId.get();
        Integer lastMemberTotalPrice = savedExpenses.stream()
            .filter(expense -> expense.getMember().getId().equals(lastUpdatedMemberId))
            .map(PersonalExpense::getTotalPrice).findFirst().orElse(0);

        assertEquals(3, savedExpenses.size());
        assertTrue(
            savedExpenses.stream().anyMatch(pe -> pe.getMember().getId().equals(member1.getId())));
        assertTrue(
            savedExpenses.stream().anyMatch(pe -> pe.getMember().getId().equals(member2.getId())));
        assertTrue(
            savedExpenses.stream().anyMatch(pe -> pe.getMember().getId().equals(member3.getId())));

        assertEquals(expectedTotalPrices, actualTotalPrices);   // 전체 값 검증
        assertEquals(expectedTotalPrices.get(2), lastMemberTotalPrice); // 마지막에 완료 된 값 668 검증
    }

    @Test
    @DisplayName("개인 소비 내역 저장 - 기존 데이터 업데이트 테스트")
    void updatePersonalExpenseTest() {

        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 1)));

        // when
        personalExpenseService.savePersonalExpense(member2.getId(), team.getId(), expense.getId(),
            request);

        // then
        List<PersonalExpense> savedExpenses = personalExpenseRepository.findAllByItem(item2);
        assertEquals(2, savedExpenses.size());

        PersonalExpense personalExpense1 = savedExpenses.get(0);
        PersonalExpense personalExpense2 = savedExpenses.get(1);

        assertEquals(personalExpense1.getQuantity(), personalExpense2.getQuantity());
        assertEquals(personalExpense1.getTotalPrice(), personalExpense2.getTotalPrice());
    }

    @Test
    @DisplayName("개인 소비 내역 저장 - 예외 발생 테스트")
    void savePersonalExpenseExceptionTest() {

        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new ItemInfo(item2.getId(), 10))
        );

        // when, then
        assertThrows(JeongsanException.class, () -> {
            personalExpenseService.savePersonalExpense(member1.getId(), team.getId(),
                expense.getId(), request);
        });

        List<PersonalExpense> savedExpenses = personalExpenseRepository.findAll();
        assertEquals(1, savedExpenses.size());
    }
}
