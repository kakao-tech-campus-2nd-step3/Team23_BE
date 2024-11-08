package kappzzang.jeongsan.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.dto.CategoryDto;
import kappzzang.jeongsan.global.common.enumeration.Status;

public record ExpenseResponse(
    List<ExpenseItem> expenseList,
    Boolean checked,
    Integer totalPrice
) {

    public static ExpenseResponse of(List<Expense> expenseList, Boolean isChecked,
        Integer totalPrice) {
        List<ExpenseItem> expenseItems = expenseList.stream()
            .map(ExpenseItem::from)
            .toList();
        return new ExpenseResponse(expenseItems, isChecked, totalPrice);
    }

    public record ExpenseItem(
        Long expenseId,
        String title,
        Integer totalPrice,
        LocalDateTime createdAt,
        Status state,
        CategoryDto category
    ) {

        public static ExpenseItem from(Expense expense) {
            return new ExpenseItem(
                expense.getId(),
                expense.getTitle(),
                expense.getTotalPrice(),
                expense.getCreatedAt(),
                expense.getStatus(),
                CategoryDto.from(expense.getCategory())
            );
        }
    }
}
