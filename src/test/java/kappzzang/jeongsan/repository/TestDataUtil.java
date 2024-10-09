package kappzzang.jeongsan.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoToken;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TestDataUtil {

    TestEntityManager entityManager;

    @Autowired
    public TestDataUtil(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Team createAndPersistTeam() {
        Team team = new Team();
        entityManager.persist(team);
        return team;
    }

    public KakaoToken createAndPersistKakakoToken() {
        KakaoToken kakaoToken = new KakaoToken();
        entityManager.persist(kakaoToken);
        return kakaoToken;
    }

    public Category createAndPersistCategory() {
        Category category = new Category();
        entityManager.persist(category);
        return category;
    }

    public Member createAndPersistMember(Long id, String nickname, KakaoToken kakaoToken) {
        Member member = Member.builder()
            .id(id)
            .email("TEST_EMAIL")
            .kakaoToken(kakaoToken)
            .nickname(nickname)
            .profileImage("TEST_PROFILE_URL")
            .teamMemberList(new ArrayList<>())
            .build();
        entityManager.persist(member);
        return member;
    }

    public Expense createAndPersistExpense(Team team, Member payer, Category category,
        List<Item> items) {
        Expense expense = Expense.builder()
            .team(team)
            .member(payer)
            .title("TEST_TITLE")
            .category(category)
            .paymentTime(LocalDateTime.now())
            .imageUrl("TEST_IMAGE_URL")
            .items(items)
            .build();
        entityManager.persist(expense);
        return expense;
    }

    public PersonalExpense createAndPersistPersonalExpense(Member member,
        Integer consumedQuantity) {
        PersonalExpense personalExpense = PersonalExpense.builder()
            .member(member)
            .quantity(consumedQuantity)
            .build();
        entityManager.persist(personalExpense);
        return personalExpense;
    }

    public Item createAndPersistItem(String name, Integer price, Integer quantity) {
        Item item = new Item(name, price, quantity);
        entityManager.persist(item);
        return item;
    }

    public void commit() {
        entityManager.flush();
    }

}
