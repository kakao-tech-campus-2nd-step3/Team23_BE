package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.dto.ItemDetail;
import kappzzang.jeongsan.dto.response.PersonalExpenseDetailResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    private static final Long TEST_EXPENSE_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_IMAGE_URL = "TEST_IMAGE_URL";
    private final String TEST_PRE_SIGNED_URL = "TEST_PRE_SIGNED_URL";
    private final String TEST_TITLE = "TEST_TITLE";

    @Mock
    private ImageStorageService mockImageStorageService;

    @Mock
    private ExpenseRepository mockExpenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
            .title(TEST_TITLE)
            .imageUrl(TEST_IMAGE_URL)
            .items(List.of(new Item("TEST_NAME", 10, 10)))
            .build();
    }

    @Test
    @DisplayName("개인 지출 목록 조회 성공 테스트")
    void testGetPersonalExpenseDetailResponseSuccess() {
        //given
        ItemDetail itemDetailA = new ItemDetail(1L, "TEST_ITEM_A", 10, 1000, 10);
        ItemDetail itemDetailB = new ItemDetail(2L, "TEST_ITEM_B", 5, 5000, 0);
        ItemDetail itemDetailC = new ItemDetail(3L, "TEST_ITEM_C", 3, 2000, 5);
        List<ItemDetail> itemDetails = List.of(itemDetailA, itemDetailB, itemDetailC);
        PersonalExpenseDetailResponse expected = new PersonalExpenseDetailResponse(
            expense.getTitle(), TEST_PRE_SIGNED_URL, itemDetails);

        given(mockExpenseRepository.findById(TEST_EXPENSE_ID)).willReturn(
            Optional.ofNullable(expense));
        given(mockExpenseRepository.findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID)).willReturn(
            itemDetails);
        given(mockImageStorageService.getImageUrl(TEST_IMAGE_URL)).willReturn(TEST_PRE_SIGNED_URL);

        //when
        PersonalExpenseDetailResponse actual = expenseService.getPersonalExpenseDetailResponse(
            TEST_EXPENSE_ID,
            TEST_MEMBER_ID);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(mockExpenseRepository).findById(TEST_EXPENSE_ID);
        verify(mockExpenseRepository).findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID);
        verify(mockImageStorageService).getImageUrl(TEST_IMAGE_URL);
    }

    @Test
    @DisplayName("개인 지출 목록 조회 실패(존재하지 않는 지출 ID)")
    void testGetPersonalExpenseDetailResponseFailWithNotFoundExpense() {
        //given
        given(mockExpenseRepository.findById(TEST_EXPENSE_ID)).willThrow(new JeongsanException(
            ErrorType.EXPENSE_NOT_FOUND));

        //when //then
        assertThatThrownBy(
            () -> expenseService.getPersonalExpenseDetailResponse(TEST_EXPENSE_ID, TEST_MEMBER_ID)
        )
            .isInstanceOf(JeongsanException.class)
            .hasMessage(ErrorType.EXPENSE_NOT_FOUND.getMessage());
        verify(mockExpenseRepository).findById(TEST_EXPENSE_ID);
    }

    @Test
    @DisplayName("개인 지출 목록 조회 실패(존재하지 않는 맴버 or 지출 품목이 존재하지 않는 경우)")
    void testGetPersonalExpenseDetailResponseFailWithReturnEmptyList() {
        //given
        given(mockExpenseRepository.findById(TEST_EXPENSE_ID)).willReturn(
            Optional.ofNullable(expense));
        given(mockExpenseRepository.findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID)).willReturn(List.of());

        //when //then
        assertThatThrownBy(
            () -> expenseService.getPersonalExpenseDetailResponse(TEST_EXPENSE_ID,
                TEST_MEMBER_ID)).isInstanceOf(
            JeongsanException.class).hasMessage(ErrorType.PERSONAL_EXPENSE_NOT_FOUND.getMessage());
        verify(mockExpenseRepository).findById(TEST_EXPENSE_ID);
        verify(mockExpenseRepository).findItemDetailsByExpenseIdAndMemberId(TEST_EXPENSE_ID,
            TEST_MEMBER_ID);
    }

}
