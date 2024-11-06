package kappzzang.jeongsan.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.dto.CategoryDto;
import kappzzang.jeongsan.global.common.enumeration.Status;

public record ExpenseResponse(
    List<ExpenseItem> expenseList,
    Boolean checked,
    Integer totalPrice,
    Integer totalPersonalExpense
) {

    public static ExpenseResponse of(List<Expense> expenseList, Boolean isChecked,
        Integer totalPrice, Integer personalExpensePrice) {

        List<ExpenseItem> expenseItems = expenseList.stream()
            .map(expense -> ExpenseItem.from(expense, personalExpensePrice))
            .toList();

        int totalPersonalExpense = expenseItems.stream()
            .mapToInt(ExpenseItem::personalExpense)
            .sum();

        return new ExpenseResponse(expenseItems, isChecked, totalPrice, totalPersonalExpense);
    }

    public record ExpenseItem(
        Long expenseId,
        String title,
        Integer totalPrice,
        LocalDateTime createdAt,
        Status state,
        CategoryDto category,
        Integer personalExpense
    ) {

        public static ExpenseItem from(Expense expense, Integer personalExpensePrice) {
            return new ExpenseItem(
                expense.getId(),
                expense.getTitle(),
                expense.getTotalPrice(),
                expense.getCreatedAt(),
                expense.getStatus(),
                CategoryDto.from(expense.getCategory()),
                personalExpensePrice
            );
        }
    }
}
