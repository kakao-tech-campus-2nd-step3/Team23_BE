package kappzzang.jeongsan.dto;

import kappzzang.jeongsan.domain.Expense;

public record ExpenseWithPersonalExpense(Expense expense, Integer personalExpense) {

    public static ExpenseWithPersonalExpense of(Expense expense, Integer personalExpense) {
        return new ExpenseWithPersonalExpense(expense, personalExpense);
    }
}
