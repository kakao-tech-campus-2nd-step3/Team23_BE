package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.response.ExpenseDetailResponse;
import kappzzang.jeongsan.dto.response.ExpenseDetailResponse.ItemDetailWithPersonal;
import kappzzang.jeongsan.dto.response.ExpenseDetailResponse.ItemDetailWithPersonal.PersonalDetail;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetExpenseDetailResponseTest {

    private static final String EXPENSE_NAME = "TEST_EXPENSE_NAME";
    private static final String EXPENSE_IMAGE = "TEST_EXPENSE_IMAGE";

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PersonalExpenseRepository personalExpenseRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ExpenseService expenseService;

    private List<Member> members;
    private List<Item> items;
    private Expense expense;

    @DisplayName("유효한 결제자로 조회할 때")
    @Nested
    class ValidPayerTest {

        @BeforeEach
        void setUp() {
            members = new ArrayList<>();
            items = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Member member = mock(Member.class);
                given(member.getNickname()).willReturn((i == 0) ? "payer" : ("member" + i));
                given(member.getProfileImage()).willReturn("testProfileImage" + i);
                members.add(member);
            }

            for (int i = 0; i < 5; i++) {
                Item item = mock(Item.class);
                given(item.getId()).willReturn((long) i);
                given(item.getName()).willReturn("item" + i);
                given(item.getUnitPrice()).willReturn((i + 1) * 1000);
                given(item.getQuantity()).willReturn((i + 1) * 5);
                items.add(item);
            }

            expense = Expense.builder()
                .member(members.getFirst())
                .items(items)
                .team(mock(Team.class))
                .title(EXPENSE_NAME)
                .imageUrl(EXPENSE_IMAGE)
                .paymentTime(LocalDateTime.now())
                .category(mock(Category.class))
                .build();
        }

        @DisplayName("지출의 선택 상세 정보를 반환한다")
        @Test
        void getExpenseDetailResponse_ShouldReturnExpenseDetailResponse() {
            //given
            // item0 선택(payer, member1, member3)
            PersonalExpense pe1 = createPersonalExpense(items.get(0), members.get(0), 2);
            PersonalExpense pe2 = createPersonalExpense(items.get(0), members.get(1), 3);
            PersonalExpense pe3 = createPersonalExpense(items.get(0), members.get(3), 1);

            // item1 선택(payer, member1, member2)
            PersonalExpense pe4 = createPersonalExpense(items.get(1), members.get(0), 2);
            PersonalExpense pe5 = createPersonalExpense(items.get(1), members.get(1), 3);
            PersonalExpense pe6 = createPersonalExpense(items.get(1), members.get(2), 4);

            // item2 선택(member3)
            PersonalExpense pe7 = createPersonalExpense(items.get(2), members.get(3), 5);

            // item3 선택(member1, member4)
            PersonalExpense pe8 = createPersonalExpense(items.get(3), members.get(1), 2);
            PersonalExpense pe9 = createPersonalExpense(items.get(3), members.get(4), 3);

            List<PersonalExpense> personalExpenses = List.of(pe1, pe2, pe3, pe4, pe5, pe6, pe7, pe8,
                pe9);

            given(expenseRepository.findExpenseByIdWithItem(0L))
                .willReturn(Optional.ofNullable(expense));
            given(personalExpenseRepository.findAllByItemIds(any()))
                .willReturn(personalExpenses);
            given(imageStorageService.getImageUrl(expense.getImageUrl()))
                .willReturn(EXPENSE_IMAGE);

            //when
            ExpenseDetailResponse actual = expenseService.getExpenseDetailResponse(0L, 0L);

            //then
            ExpenseDetailResponse expected = createExpectedResponse();
            assertThat(actual).isEqualTo(expected);
        }


        private ExpenseDetailResponse createExpectedResponse() {
            List<ItemDetailWithPersonal> itemDetails = List.of(
                new ItemDetailWithPersonal(0L, "item0", 5, 1000, List.of(
                    new PersonalDetail("payer", "testProfileImage0", 2),
                    new PersonalDetail("member1", "testProfileImage1", 3),
                    new PersonalDetail("member3", "testProfileImage3", 1)
                )),
                new ItemDetailWithPersonal(1L, "item1", 10, 2000, List.of(
                    new PersonalDetail("payer", "testProfileImage0", 2),
                    new PersonalDetail("member1", "testProfileImage1", 3),
                    new PersonalDetail("member2", "testProfileImage2", 4)
                )),
                new ItemDetailWithPersonal(2L, "item2", 15, 3000, List.of(
                    new PersonalDetail("member3", "testProfileImage3", 5)
                )),
                new ItemDetailWithPersonal(3L, "item3", 20, 4000, List.of(
                    new PersonalDetail("member1", "testProfileImage1", 2),
                    new PersonalDetail("member4", "testProfileImage4", 3)
                )),
                new ItemDetailWithPersonal(4L, "item4", 25, 5000, List.of())
            );

            return new ExpenseDetailResponse(EXPENSE_NAME, EXPENSE_IMAGE, itemDetails);
        }

        private PersonalExpense createPersonalExpense(Item item, Member member, int quantity) {
            PersonalExpense personalExpense = mock(PersonalExpense.class);
            given(personalExpense.getItem()).willReturn(item);
            given(personalExpense.getQuantity()).willReturn(quantity);
            given(personalExpense.getMember()).willReturn(member);
            return personalExpense;
        }
    }

    @DisplayName("유효하지 않은 결제자로 조회할 때")
    @Nested
    class InvalidPayerTest {

        @BeforeEach
        void setUp() {
            Member payer = mock(Member.class);
            given(payer.getId())
                .willReturn(1L);
            expense = Expense.builder()
                .title(EXPENSE_NAME)
                .team(mock(Team.class))
                .category(mock(Category.class))
                .items(List.of(mock(Item.class)))
                .paymentTime(LocalDateTime.now())
                .member(payer)
                .imageUrl(EXPENSE_IMAGE)
                .build();
        }

        @DisplayName("InvalidPayer 에러 코드가 담긴 예외를 던진다")
        @Test
        void getExpenseDetailResponse_WithInvalidPayer_ThrowsInvalidPayerException() {
            //given
            given(expenseRepository.findExpenseByIdWithItem(0L))
                .willReturn(Optional.ofNullable(expense));

            //when //then
            assertThatThrownBy(() -> expenseService.getExpenseDetailResponse(0L, 100L))
                .isInstanceOf(JeongsanException.class)
                .hasMessage(ErrorType.EXPENSE_INVALID_PAYER.getMessage());
        }
    }
}
