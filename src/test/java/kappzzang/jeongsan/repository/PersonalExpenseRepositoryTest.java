package kappzzang.jeongsan.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestDataUtil.class)
class PersonalExpenseRepositoryTest {

    Member member1, member2;
    Expense expense1, expense2;
    PersonalExpense personalExpense1, personalExpense2, personalExpense3;
    private List<Long> itemIds;
    @Autowired
    private PersonalExpenseRepository personalExpenseRepository;
    @Autowired
    private TestDataUtil testDataUtil;

    @BeforeEach
    void setUp() {
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo();
        Team team = testDataUtil.createAndPersistTeam();
        Category category = testDataUtil.createAndPersistCategory();
        Item item1 = testDataUtil.createAndPersistItem("item1", 10, 2000);
        Item item2 = testDataUtil.createAndPersistItem("item2", 10, 2000);
        List<Item> items1 = List.of(item1);
        List<Item> items2 = List.of(item2);
        itemIds = List.of(item1.getId(), item2.getId());
        member1 = testDataUtil.createAndPersistMember("member1", kakaoPayInfo);
        member2 = testDataUtil.createAndPersistMember("member2", kakaoPayInfo);
        expense1 = testDataUtil.createAndPersistExpense(team, member1, category, items1);
        expense2 = testDataUtil.createAndPersistExpense(team, member2, category, items2);
        personalExpense1 = testDataUtil.createAndPersistPersonalExpense(member1, 1,
            item1, 1);
        personalExpense2 = testDataUtil.createAndPersistPersonalExpense(member2, 1,
            item1, 1);
        personalExpense3 = testDataUtil.createAndPersistPersonalExpense(member1, 1,
            item2, 1);
    }

    @Test
    @DisplayName("ExpenseId를 입력받아 해당 지출과 연관된 PersonalExpense를 반환한다.")
    void findAllByExpenseIds() {
        // given
        List<Long> expenseIds = Arrays.asList(expense1.getId(), expense2.getId());
        // when
        List<PersonalExpense> results = personalExpenseRepository.findAllByExpenseIds(
            expenseIds);
        // then
        assertThat(results).containsExactlyInAnyOrder(personalExpense1, personalExpense2,
            personalExpense3);
    }

    @DisplayName("아이템 Id 목록으로 회원과 아이템이 포함된 개인 지출 목록을 조회한다")
    @Test
    void findAllByItemIds_WithItemIds_ReturnPersonalExpenses() {
        //when
        List<PersonalExpense> actual = personalExpenseRepository.findAllByItemIds(itemIds);

        //then
        assertThat(actual)
            .hasSize(3)
            .allSatisfy(personalExpense -> {
                assertThat(personalExpense.getId()).isNotNull();
                assertThat(personalExpense.getMember()).isNotNull();
                assertThat(personalExpense.getItem()).isNotNull();
                assertThat(itemIds).contains(personalExpense.getItem().getId());
            });
    }

}
