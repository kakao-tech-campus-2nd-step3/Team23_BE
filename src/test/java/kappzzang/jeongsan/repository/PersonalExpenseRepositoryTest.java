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
    Item item1, item2, item3;
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
        item1 = testDataUtil.createAndPersistItem("item1", 100, 5);
        item2 = testDataUtil.createAndPersistItem("item2", 1000, 7);
        item3 = testDataUtil.createAndPersistItem("item3", 2000, 10);
        List<Item> items1 = List.of(item1);
        List<Item> items2 = List.of(item2, item3);
        itemIds = List.of(item1.getId(), item2.getId());
        member1 = testDataUtil.createAndPersistMember("member1", kakaoPayInfo);
        member2 = testDataUtil.createAndPersistMember("member2", kakaoPayInfo);
        expense1 = testDataUtil.createAndPersistExpense(team, member1, category, items1);
        expense2 = testDataUtil.createAndPersistExpense(team, member2, category, items2);
        personalExpense1 = testDataUtil.createAndPersistPersonalExpense(member1, 1,
            item1, 100);
        personalExpense2 = testDataUtil.createAndPersistPersonalExpense(member2, 1,
            item1, 100);
        personalExpense3 = testDataUtil.createAndPersistPersonalExpense(member1, 1,
            item2, 1000);
    }

    @Test
    @DisplayName("지출과 연관된 개인 소비 내역을 반환하되 소비 수량이 0인 소비 내역은 포함하지 않는다.")
    void findAllByExpenseIds() {
        // given
        PersonalExpense personalExpense4 = testDataUtil.createAndPersistPersonalExpense(member2, 0,
            item2, 0);
        List<Long> expenseIds = Arrays.asList(expense1.getId(), expense2.getId());

        // when
        List<PersonalExpense> results = personalExpenseRepository.findAllByExpenseIds(
            expenseIds);

        // then
        assertThat(results).doesNotContain(personalExpense4);
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

    @Test
    @DisplayName("지출 Id와 멤버 Id로 사용자의 개인 소비 총 금액의 합을 조회한다")
    void findPersonalExpenseSum() {
        // given
        PersonalExpense personalExpense4 = testDataUtil.createAndPersistPersonalExpense(member1, 2,
            item3, 4000);

        // when
        int actual = personalExpenseRepository.findPersonalExpenseSum(expense2.getId(),
            member1.getId());

        // then
        assertThat(actual).isEqualTo(
            personalExpense1.getTotalPrice() + personalExpense4.getTotalPrice());
    }
}
