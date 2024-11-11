package kappzzang.jeongsan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties")
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

    private Member member1, member2, member3, member4;
    private Team team;
    private Expense expense;
    private Item item1, item2;

    @BeforeEach
    void setup() {
        team = teamRepository.save(new Team("Test Team", "🍎"));
        member1 = memberRepository.save(
            Member.builder()
                .kakaoId("kakaoId1")
                .email("email1@test.com")
                .nickname("User1")
                .build());
        member2 = memberRepository.save(
            Member.builder()
                .kakaoId("kakaoId2")
                .email("email2@test.com")
                .nickname("User2")
                .build());
        member3 = memberRepository.save(
            Member.builder()
                .kakaoId("kakaoId3")
                .email("email3@test.com")
                .nickname("User3")
                .build());
        member4 = memberRepository.save(
            Member.builder()
                .kakaoId("kakaoId4")
                .email("email4@test.com")
                .nickname("User4")
                .build());
        item1 = new Item("Test Item", 2, 1000);
        item2 = new Item("Test Item", 3, 1000);
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
        teamMemberRepository.save(new TeamMember(member4, team, false, true));
    }

    @AfterEach
    void cleanup() {
        personalExpenseRepository.deleteAll();
        itemRepository.deleteAll();
        expenseRepository.deleteAll();
        teamRepository.deleteAll();
        memberRepository.deleteAll();
        teamMemberRepository.deleteAll();
    }

    @Test
    @DisplayName("3명이 동시에 개인 소비 내역을 저장하면,"
        + "차례로 처리 되도록 하고, "
        + "마지막에 처리된 요청이 나머지 금액을 부담한다.")
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
    @DisplayName("기존 개인 소비 내역 데이터와 다른 수량으로 요청 시 "
        + "개인 소비 내역이 업데이트 된다.")
    void updatePersonalExpenseTest() {

        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 2)));

        // when
        personalExpenseService.savePersonalExpense(member1.getId(), team.getId(), expense.getId(),
            request);

        // then
        PersonalExpense savedExpenses = personalExpenseRepository.findByMemberAndItem(member1,
            item2).get();

        assertEquals(2, savedExpenses.getQuantity());
        assertEquals(2000, savedExpenses.getTotalPrice());
    }

    @Test
    @DisplayName("아이탬의 수량보다 많은 수량이 포함된 요청은 예외가 발생한다.")
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

    @Test
    @DisplayName("기존 개인 소비 내역 데이터와 동일한 수량이 포함된 요청은 예외가 발생한다.")
    void updatePersonalExpenseWithSameQuantityTest() {
        // given
        SavePersonalExpenseRequest request = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 1))
        );

        // when, then
        assertThrows(JeongsanException.class, () -> {
            personalExpenseService.savePersonalExpense(member1.getId(), team.getId(),
                expense.getId(), request);
        });

        PersonalExpense savedExpenses = personalExpenseRepository.findByMemberAndItem(member1,
            item2).get();
        assertEquals(1, savedExpenses.getQuantity());
        assertEquals(1000, savedExpenses.getTotalPrice());
    }

    @Test
    @DisplayName("개인 소비 내역 수정 요청 시, 요청 수량이 0이라면 기존 데이터를 삭제한다.")
    void updatePersonalExpenseWithZeroQuantityTest() {
        // given
        SavePersonalExpenseRequest updateRequest = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 0))
        );

        // when
        personalExpenseService.savePersonalExpense(member1.getId(), team.getId(), expense.getId(),
            updateRequest);

        // then
        Optional<PersonalExpense> personalExpense = personalExpenseRepository.findByMemberAndItem(
            member1, item2);
        assertTrue(personalExpense.isEmpty());
    }

    @Test
    @DisplayName("개인 소비 내역 저장 요청 시, 요청 수량이 0이라면 예외가 발생한다.")
    void savePersonalExpenseWithZeroQuantityTest() {
        // given
        SavePersonalExpenseRequest saveRequest = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item1.getId(), 0))
        );

        // when, then
        assertThrows(JeongsanException.class, () -> {
            personalExpenseService.savePersonalExpense(member4.getId(), team.getId(),
                expense.getId(), saveRequest);
        });

        Optional<PersonalExpense> personalExpense = personalExpenseRepository.findByMemberAndItem(
            member4, item1);
        assertTrue(personalExpense.isEmpty());
    }

    @Test
    @DisplayName("개인 소비 내역 저장, 수정 요청이 동시에 요청된다.")
    void concurrentUpdateAndSaveTest() throws InterruptedException {
        // given
        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        SavePersonalExpenseRequest updateRequest = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 2))
        );
        SavePersonalExpenseRequest saveRequest = new SavePersonalExpenseRequest(
            List.of(new SavePersonalExpenseRequest.ItemInfo(item2.getId(), 2))
        );

        List<TestCase> testCases = List.of(
            new TestCase(member1.getId(), "수정", updateRequest),
            new TestCase(member4.getId(), "저장", saveRequest)
        );

        // when
        testCases.forEach(testCase ->
            executorService.submit(() -> executeTestCase(testCase, startLatch, endLatch))
        );

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // then
        List<PersonalExpense> savedExpenses = personalExpenseRepository.findAllByItem(item2);
        assertEquals(2, savedExpenses.size());

        int totalQuantity = savedExpenses.stream()
            .mapToInt(PersonalExpense::getQuantity)
            .sum();
        assertEquals(4, totalQuantity);  // member1: 2, member4: 2

        int totalPrice = savedExpenses.stream()
            .mapToInt(PersonalExpense::getTotalPrice)
            .sum();
        assertEquals(item2.getTotalPrice(), totalPrice);  // item2의 전체 가격

        for (PersonalExpense personalExpense : savedExpenses) {
            assertEquals(2, personalExpense.getQuantity());
            assertEquals(1500, personalExpense.getTotalPrice());
        }
    }

    private void executeTestCase(TestCase testCase, CountDownLatch startLatch,
        CountDownLatch endLatch) {
        try {
            startLatch.await();
            personalExpenseService.savePersonalExpense(
                testCase.memberId(),
                team.getId(),
                expense.getId(),
                testCase.request()
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endLatch.countDown();
        }
    }

    private record TestCase(Long memberId, String operation, SavePersonalExpenseRequest request) {

    }
}
