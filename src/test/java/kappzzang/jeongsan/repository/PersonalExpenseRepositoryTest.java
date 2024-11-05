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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
@DataJpaTest
@Import(TestDataUtil.class)
class PersonalExpenseRepositoryTest {
    @Autowired
    private PersonalExpenseRepository personalExpenseRepository;
    @Autowired
    private TestDataUtil testDataUtil;
    Member member1, member2;
    Expense expense1, expense2;
    PersonalExpense personalExpense1, personalExpense2, personalExpense3;
    @BeforeEach
    void setUp() {
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo();
        Team team = testDataUtil.createAndPersistTeam();
        Category category = testDataUtil.createAndPersistCategory();
        Item item1 = testDataUtil.createAndPersistItem("item1", 10, 2000);
        Item item2 = testDataUtil.createAndPersistItem("item2", 10, 2000);
        List<Item> items1 = List.of(item1);
        List<Item> items2 = List.of(item2);
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
    void findAllByExpenseIdsWithItemAndMember() {
        // given
        List<Long> expenseIds = Arrays.asList(expense1.getId(), expense2.getId());
        // when
        List<PersonalExpense> results = personalExpenseRepository.findAllByExpenseIdsWithItemAndMember(
            expenseIds);
        // then
        assertThat(results).containsExactlyInAnyOrder(personalExpense1, personalExpense2, personalExpense3);
    }
}
