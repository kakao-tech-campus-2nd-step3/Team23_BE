package kappzzang.jeongsan.repository;

import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TestDataUtil {

    private static final String DEFAULT_KAKAO_ID = "DEFAULT_KAKAO_ID";
    private static final String DEFAULT_NAME = "DEFAULT_NAME";
    private static final String DEFAULT_COLOR = "DEFAULT_COLOR";
    private static final String DEFAULT_SUBJECT = "DEFAULT_SUBJECT";
    private static final String DEFAULT_EMAIL = "DEFAULT_EMAIL";
    private static final String DEFAULT_URL = "DEFAULT_URL";

    private final TestEntityManager entityManager;

    @Autowired
    public TestDataUtil(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    //Team
    public Team createAndPersistTeam() {
        Team team = new Team(DEFAULT_NAME, DEFAULT_SUBJECT);
        entityManager.persist(team);
        return team;
    }

    //Category
    public Category createAndPersistCategory() {
        Category category = new Category(DEFAULT_NAME, DEFAULT_COLOR);
        entityManager.persist(category);
        return category;
    }

    //Member
    public Member createAndPersistMember(String nickname, KakaoPayInfo kakaoPayInfo) {
        Member member = Member.builder()
            .kakaoId(DEFAULT_KAKAO_ID)
            .email(DEFAULT_EMAIL)
            .nickname(nickname)
            .profileImage(DEFAULT_URL)
            .kakaoPayInfo(kakaoPayInfo)
            .build();
        entityManager.persist(member);
        return member;
    }

    //Expense
    public Expense createAndPersistExpense(Team team, Member payer, Category category,
        List<Item> items) {
        Expense expense = Expense.builder()
            .team(team)
            .member(payer)
            .title(DEFAULT_NAME)
            .category(category)
            .paymentTime(LocalDateTime.now())
            .imageUrl(DEFAULT_URL)
            .items(items)
            .build();
        entityManager.persist(expense);
        return expense;
    }

    //PersonalExpense
    public PersonalExpense createAndPersistPersonalExpense(Member member,
        Integer consumedQuantity, Item item) {
        PersonalExpense personalExpense = PersonalExpense.builder()
            .member(member)
            .quantity(consumedQuantity)
            .item(item)
            .build();
        entityManager.persist(personalExpense);
        return personalExpense;
    }

    //Item
    public Item createAndPersistItem(String name, Integer price, Integer quantity) {
        Item item = new Item(name, price, quantity);
        entityManager.persist(item);
        return item;
    }

    //TeamMember
    public TeamMember createAndPersistTeamMember(Member member, Team team, Boolean isOwner,
        Boolean isInviteAccepted) {
        TeamMember teamMember = new TeamMember(member, team, isOwner, isInviteAccepted);
        entityManager.persist(teamMember);
        return teamMember;
    }

    public void commit() {
        entityManager.flush();
    }

}
