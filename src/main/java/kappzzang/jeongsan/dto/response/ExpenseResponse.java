package kappzzang.jeongsan.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.dto.CategoryDto;
import kappzzang.jeongsan.dto.ExpenseWithPersonalExpense;
import kappzzang.jeongsan.global.common.enumeration.Status;

public record ExpenseResponse(
    List<ExpenseItem> expenseList,
    Boolean checked,
    Integer totalPrice,
    Integer totalPersonalExpense
) {

    public static ExpenseResponse of(List<ExpenseWithPersonalExpense> expenseList,
        Boolean isChecked, Integer totalPrice) {

        List<ExpenseItem> expenseItems = expenseList.stream()
            .map(ExpenseItem::from)
            .toList();

        Integer totalPersonalExpense = expenseItems.stream()
            .map(ExpenseItem::personalExpense)
            .reduce(0, ExpenseResponse::sumPersonalExpenses);

        return new ExpenseResponse(expenseItems, isChecked, totalPrice, totalPersonalExpense);
    }

    private static Integer sumPersonalExpenses(Integer subtotal, Integer personalExpense) {
        if (personalExpense == null) {
            return null;
        }
        return subtotal + personalExpense;
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

        public static ExpenseItem from(ExpenseWithPersonalExpense expenseWithPersonalExpense) {
            Expense expense = expenseWithPersonalExpense.expense();
            return new ExpenseItem(
                expense.getId(),
                expense.getTitle(),
                expense.getTotalPrice(),
                expense.getCreatedAt(),
                expense.getStatus(),
                CategoryDto.from(expense.getCategory()),
                expenseWithPersonalExpense.personalExpense()
            );
        }
    }
}
